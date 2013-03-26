package EnterpriseSystems.CloudManager.DroolsManagement;

import java.util.*;

import EnterpriseSystems.CloudManager.Model.*;
import EnterpriseSystems.CloudManager.Server.CloudCommunicator;
import org.apache.log4j.Logger;

public class DroolsManager {

    private static final Logger LOG = Logger.getLogger(DroolsManager.class);
    private Map<Long, Blade> blades;
    private DroolsConfiguration droolsConfiguration;
    private DecisionBuilder decisionBuilder;

    public DroolsManager(CloudCommunicator cloudCommunicator) {
        blades = new HashMap<Long, Blade>();
        droolsConfiguration = new DroolsConfiguration();
        droolsConfiguration.Init(this);
        decisionBuilder = new DecisionBuilder(cloudCommunicator);
    }

    public synchronized void addObjectsToModel(Cloud newBladeData) {
        System.out.println("");
        System.out.println("recieved data");
        System.out.println("");

        this.blades = newBladeData.getBlades();
        droolsConfiguration.addToEngine(newBladeData);
//        for (Blade blade : newBladeData.getBlades().values()) {
//            System.out.println(blade.getAvCpuUsage());
//        }
//        System.out.println("");
    }

    public synchronized void tryToShutDownBlade(Blade droolsBlade) {
        Blade bladeToShutdown = blades.get(droolsBlade.getID());
        List<VM> vmsOnBlade = new ArrayList<VM>();

        //use copy constructor to clone the vm list on the blade
        for (VM vm : bladeToShutdown.getVMs().values())
            vmsOnBlade.add(new VM(vm));

        //try to remove vms from our cloned list
        for (Blade targetBlade : getDecSortedBladeList(Overload.CPU, bladeToShutdown)) {

            Iterator<VM> vmIterator = vmsOnBlade.iterator();

            //have to do it this way to avoid concurrent mod exception
            while (vmIterator.hasNext()) {
                VM vm = vmIterator.next();
                if (targetBlade.isMoveOk(vm)) {
                    vmIterator.remove();
                    decisionBuilder.moveVm2Blade(vm, targetBlade, bladeToShutdown);
                }
            }

        }

        //if managed to move all blades off then success!
        if (vmsOnBlade.isEmpty()) {
            System.out.println("shutting down blade " + bladeToShutdown.getID());
            decisionBuilder.shutdownBlade(bladeToShutdown);
            decisionBuilder.sendToClient();
        } else {
            decisionBuilder.clearDecisions();
            System.out.println("couldnt should down blade " + bladeToShutdown.getID() + " couldnt remove:");
            for (VM vm : vmsOnBlade) {
                System.out.println("VM ID: " + vm.getID());
            }
        }


    }

    public synchronized void allocateOverloadedVM(Blade droolsOverLoadedBlade, Overload factor) {
        System.out.println("running overload algo");
        Blade overLoadedBlade = blades.get(droolsOverLoadedBlade.getID());
        boolean moveSuccessful = false;

        //First Pass, try not to load up blades that could be shut down
        for (Blade targetBlade : getDecSortedBladeList(factor, overLoadedBlade)) {
            if (moveSuccessful)
                break;

            for (VM vm : overLoadedBlade.getDecSortedVMList(factor)) {

                if (targetBlade.isMoveOk(vm)) {
                    if (overLoadedBlade.isNoLongerOverloaded(vm)) {
                        decisionBuilder.moveVm2Blade(vm, targetBlade, overLoadedBlade);
                        moveSuccessful = true;
                        break;
                    } else {
                        System.out.println("not moving blade as will not fix overload");
                    }
                }
            }
        }

        if (moveSuccessful) {
            decisionBuilder.sendToClient();
            return;
        }

        //Second Pass, move any VM of the blade that will go onto another blade
        for (Blade targetBlade : getDecSortedBladeList(factor, overLoadedBlade)) {
            if (moveSuccessful)
                break;

            for (VM vm : overLoadedBlade.getDecSortedVMList(factor)) {

                if (targetBlade.isMoveOk(vm)) {
                    decisionBuilder.moveVm2Blade(vm, targetBlade, overLoadedBlade);
                    moveSuccessful = true;
                    break;
                }
            }
        }

        //Use a lower threshold so its not too hard to get to the point of opening a new blade

        //Finally try to open a new blade if no move is possible
        if (!moveSuccessful) {
            if (isBladeAvailable())
                decisionBuilder.tryOpenNewBlade();
            else
                System.out.println("System Overloaded!");
        }

        decisionBuilder.sendToClient();
    }


    private boolean isBladeAvailable() {
        for (Blade blade : this.blades.values()) {
            if (!blade.isOn())
                return true;
        }
        return false;
    }

    private List<Blade> getDecSortedBladeList(Overload factor, Blade originalBlade) {
        List<Blade> bladeList;
        switch (factor) {
            case CPU:
                bladeList = getDecSortedBladeList_CPU();
                bladeList.remove(originalBlade);
                return bladeList;
            case NETWORK:
                bladeList = getDecSortedBladeList_NETWORK();
                bladeList.remove(originalBlade);
                return bladeList;
            case MEMORY:
                bladeList = getDecSortedBladeList_MEMORY();
                bladeList.remove(originalBlade);
                return bladeList;
            case DISK:
                bladeList = getDecSortedBladeList_DISK();
                bladeList.remove(originalBlade);
                return bladeList;
            default:
                System.out.println("PROBLEM!!");
                return null;
        }
    }

    private List<Blade> getDecSortedBladeList_CPU() {
        List<Blade> bladesList = new ArrayList<Blade>(this.blades.values());
        Collections.sort(bladesList, new Comparator<Blade>() {
            @Override
            public int compare(Blade blade1, Blade blade2) {
                if (blade1.getAvCpuUsage() > blade2.getAvCpuUsage())
                    return 1;
                else
                    return -1;
            }
        });
        Collections.reverse(bladesList);
        return bladesList;
    }

    private List<Blade> getDecSortedBladeList_NETWORK() {
        List<Blade> bladesList = new ArrayList<Blade>(this.blades.values());
        Collections.sort(bladesList, new Comparator<Blade>() {
            @Override
            public int compare(Blade blade1, Blade blade2) {
                if (blade1.getAvNetworkUsage() > blade2.getAvNetworkUsage())
                    return 1;
                else
                    return -1;
            }
        });
        Collections.reverse(bladesList);
        return bladesList;
    }

    private List<Blade> getDecSortedBladeList_MEMORY() {
        List<Blade> bladesList = new ArrayList<Blade>(this.blades.values());
        Collections.sort(bladesList, new Comparator<Blade>() {
            @Override
            public int compare(Blade blade1, Blade blade2) {
                if (blade1.getMemoryUsedPercentage() > blade2.getMemoryUsedPercentage())
                    return 1;
                else
                    return -1;
            }
        });
        Collections.reverse(bladesList);
        return bladesList;
    }


    private List<Blade> getDecSortedBladeList_DISK() {
        List<Blade> bladesList = new ArrayList<Blade>(this.blades.values());
        Collections.sort(bladesList, new Comparator<Blade>() {
            @Override
            public int compare(Blade blade1, Blade blade2) {
                if (blade1.getDiskUsagePercentage() > blade2.getDiskUsagePercentage())
                    return 1;
                else
                    return -1;
            }
        });
        Collections.reverse(bladesList);
        return bladesList;
    }


}


