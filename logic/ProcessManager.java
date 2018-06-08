package logic;

import java.util.ArrayList;
import java.util.Collections;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Clase que representa el manejador de procesos. Se encarga de crear, agregar y
 * atender cada uno de los procesos de acuerdo a sus características. Su modo de
 * atención es FIFO (First Input, First Output), teniendo prelación sobre
 * aquellos procesos que no están bloqueados.
 *
 * @author Cesar Cardozo & Gabriel Amaya
 */
public class ProcessManager {

    //------------------------ Atributos -----------------------------
    public static final int DEFAULT_QUANTUM = 5;
    public static final int DEFAULT_MEMORY = 50;

    private ArrayList<Process> input_ProcessList;
    private ArrayList<Process> output_ProcessList;
    private ArrayList<Partition> output_PartitionList;
    private ArrayList<Partition> partitionsList;
    private ArrayList<Partition> currentPartitionList;
    private ArrayList<Condensation> condensations;
    private int quantum;
    private int memory;
    private int currentUsedMemory;
    private int lastAssignedProcessIndex;
    private int currentIndex;

    //------------------------ Constructores -----------------------------
    public ProcessManager() {
        this.quantum = DEFAULT_QUANTUM;
        this.memory = DEFAULT_MEMORY;
        this.currentUsedMemory = 0;
        this.currentIndex = 0;
        this.lastAssignedProcessIndex = 0;
        this.input_ProcessList = new ArrayList<>();
        this.output_ProcessList = new ArrayList<>();
        this.partitionsList = new ArrayList<>();
        this.currentPartitionList = new ArrayList<>();
        this.condensations = new ArrayList<>();
        this.output_PartitionList = new ArrayList<>();
    }

    //------------------------ Métodos -----------------------------
    /**
     * Agregar un nuevo proceso al manejador de procesos. En un principio, lo
     * agrega a la lista de procesos de entrada, luego a la lista de procesos
     * listos y por último lo despacha (lo que indica que también lo agrega a la
     * lista de procesos en ejecución). Adicionalmente revisa si el proceso está
     * bloqueado y si lo está, lo agrega a la lista de procesos bloqueados
     *
     * @param p El proceso a ser agregado
     * @return true si el proceso fue agregado, de lo contrario, false
     */
    public boolean addProcess(Process p) {
        //Busca en la lista de procesos de entrada si existe un proceso con el 
        //mismo nombre, si no, lo agrega a la lista de procesos de entrada, lista
        //procesos listos y hace la transisiónde despachado
        try {
            searchProcess(p.getName(), input_ProcessList);
            return false;
        } catch (Exception e) {
            try {
                input_ProcessList.add(p);
                return true;
            } catch (Exception ex) {
                return false;
            }
        }
    }

    /**
     * Agregar un nuevo proceso al manejador de procesos. En un principio, lo
     * agrega a la lista de procesos de entrada, luego a la lista de procesos
     * listos y por último lo despacha (lo que indica que también lo agrega a la
     * lista de procesos en ejecución). Adicionalmente revisa si el proceso está
     * bloqueado y si lo está, lo agrega a la lista de procesos bloqueados
     *
     * @param par El proceso a ser agregado
     * @return true si el proceso fue agregado, de lo contrario, false
     */
    public boolean addPartition(Partition par) {
        //Busca en la lista de procesos de entrada si existe un proceso con el 
        //mismo nombre, si no, lo agrega a la lista de procesos de entrada, lista
        //procesos listos y hace la transisiónde despachado
        try {
            searchPartition(par.getPartitionName());
            return false;
        } catch (Exception e) {
            partitionsList.add(par);
            return true;
        }
    }

    /**
     *
     * @param name El nombre el proceso
     * @param executionTime El tiempo de ejecución del proceso
     * @param processSize Tamaño ocupado por el proceso
     * @return Una nueva instancia de la clase Proceso con los datos ingresados
     */
    public static Process createProcess(String name, int processSize, int executionTime) {
        return new Process(name, processSize, executionTime);
    }

    /**
     *
     * @param partitionName
     * @param partitionSize
     * @return Una nueva instancia de la clase Proceso con los datos ingresados
     */
    public static Partition createPartition(Process pro) {
        return new Partition(pro);
    }

    public void assignProcessesInitially() {
        for (int i = lastAssignedProcessIndex; i < input_ProcessList.size(); i++) {
            Process pro = input_ProcessList.get(i);
            if (currentUsedMemory + pro.getProcessSize() <= memory) {
                lastAssignedProcessIndex++;
                Partition par = createPartition(pro);
                try {
                    partitionsList.add((Partition) par.clone());
                } catch (Exception e) {
                    e.printStackTrace();
                }
                currentPartitionList.add(par);
                currentUsedMemory += par.getPartitionSize();
            } else if (currentUsedMemory < memory) {
                Partition par = new Partition(memory - currentUsedMemory);
                try {
                    partitionsList.add((Partition) par.clone());
                } catch (Exception e) {
                    e.printStackTrace();
                }
                currentPartitionList.add(par);
                currentUsedMemory = memory;
                break;
            }
        }

    }

    public void processProcesses() {
        assignProcessesInitially();
        for (int i = 0; i < 100; i++) {
            ArrayList<Process> aux = new ArrayList<>();
            aux = execute2(aux);
            Collections.sort(aux);
            for (Process process : aux) {
                output_ProcessList.add(process);
                output_PartitionList.add(searchIntoPartition(process));
            }
        }
    }

    public Partition searchIntoPartition(Process pro) {
        for (Partition partition : partitionsList) {
            if (partition != null && partition.getPartitionSize() == pro.getProcessSize()) {
                return partition;
            }
        }
        return null;
    }

    public boolean isEmpty() {
        for (Partition partition : currentPartitionList) {
            if (partition.getAssignedProcess() != null) {
                return false;
            }
        }
        return true;
    }

    public ArrayList<Process> execute2(ArrayList<Process> auxListProcesses) {
        for (int i = 0; i < currentPartitionList.size(); i++) {
            Partition partition = currentPartitionList.get(i);
            Process pro = partition.getAssignedProcess();
            if (pro == null && (i + 1) < currentPartitionList.size() && currentPartitionList.get(i + 1).getAssignedProcess() == null) {
                condensate(i);
                continue;
                //Mira si hay procesos por asignar
            } else if (pro == null && lastAssignedProcessIndex < input_ProcessList.size()) {
                //Si el proceso a asignar cabe en el hueco actual
                int remainingMemory = partition.getPartitionSize() - input_ProcessList.get(lastAssignedProcessIndex).getProcessSize();

                //Si cabe en la partición
                if (remainingMemory >= 0) {
                    //Particion 1:
                    //Reemplaza el hueco con una nueva partición
                    Partition newPar1 = new Partition(input_ProcessList.get(lastAssignedProcessIndex));
                    //Lo asigna a la posición i si cabe exactamente, si no, en la
                    //posición i+1
                    currentPartitionList.set((remainingMemory > 0) ? i : i + 1, newPar1);
                    try {
                        partitionsList.add((Partition) newPar1.clone());
                    } catch (CloneNotSupportedException ex) {
                        ex.printStackTrace();
                    }
                    if (remainingMemory != 0) {
                        //particion 2
                        //Crea una nueva partición con o que sobre 
                        Partition newPar2 = new Partition(remainingMemory);
                        if (newPar2.getPartitionName().equals("PA12")) {
                            Partition.BASE_ID--;
                        }
                        //Agrega un nuevo hueco
                        currentPartitionList.add(i + 1, newPar2);
                        try {
                            partitionsList.add((Partition) newPar2.clone());
                        } catch (CloneNotSupportedException ex) {
                            ex.printStackTrace();
                        }
                    }
                    //Si cabe exactamente en el hueco, crea solo una partición
                    lastAssignedProcessIndex++;
                }

                //Si hay un proceso asignado a dicha partición, lo ejecuta
                //Si luego de ejecutarlo su tiempo de ejecución es menor a cero
                //enviarlo a la lista de salida temporal
            } else if (pro != null) {
                pro.setExecutionTime(pro.getExecutionTime() - quantum);
                if (pro.getExecutionTime() <= 0) {
                    auxListProcesses.add(pro);
                    partition.setAssignedProcess(null);
                }
            }
        }
        return auxListProcesses;
    }

    public ArrayList<Process> execute(ArrayList<Process> auxListProcess) {
        for (int i = 0; i < currentPartitionList.size(); i++) {
            Partition partition = currentPartitionList.get(i);
            Process pro = partition.getAssignedProcess();
            if (pro == null && (i + 1) < currentPartitionList.size() && currentPartitionList.get(i + 1).getAssignedProcess() == null) {
                condensate(i);
                continue;
                //Mira si hay procesos por asignar
            } else if (pro == null && lastAssignedProcessIndex < input_ProcessList.size()) {
                //Si el proceso a asignar cabe en el hueco actual
                if (input_ProcessList.get(lastAssignedProcessIndex).getProcessSize() < partition.getPartitionSize()) {
                    //Particion 1:
                    //Reemplaza el hueco con una nueva partición
                    Partition newPar1 = new Partition(input_ProcessList.get(lastAssignedProcessIndex));
                    currentPartitionList.set(i, newPar1);
                    try {
                        partitionsList.add((Partition) newPar1.clone());
                    } catch (CloneNotSupportedException ex) {
                        ex.printStackTrace();
                    }
                    //particion 2______________________________________________________________________________________________________________
                    //Crea una nueva partición con o que sobre 
                    Partition newPar2 = new Partition(partition.getPartitionSize() - newPar1.getPartitionSize());
                    if (newPar2.getPartitionName().equals("PA12")) {
                        System.out.println("paso por aquí");
                        Partition.BASE_ID--;
                        newPar2.setPartitionName("destroy");
                    }
                    //Agrega un nuevo hueco
                    currentPartitionList.add(i + 1, newPar2);
                    try {
                        partitionsList.add((Partition) newPar2.clone());
                    } catch (CloneNotSupportedException ex) {
                        ex.printStackTrace();
                    }
                    lastAssignedProcessIndex++;
                    //Si cabe exactamente en el hueco, crea solo una partición
                } else if (input_ProcessList.get(lastAssignedProcessIndex).getProcessSize() == partition.getPartitionSize()) {
                    Partition newPar1 = new Partition(input_ProcessList.get(lastAssignedProcessIndex));
                    currentPartitionList.set(i + 1, newPar1);
                    try {
                        partitionsList.add((Partition) newPar1.clone());
                    } catch (CloneNotSupportedException ex) {
                        ex.printStackTrace();
                    }
                    lastAssignedProcessIndex++;
                }

                //Si hay un proceso asignado a dicha partición, lo ejecuta
                //Si luego de ejecutarlo su tiempo de ejecución es menor a cero
                //enviarlo a la lista de salida temporal
            } else if (pro != null) {
                pro.setExecutionTime(pro.getExecutionTime() - quantum);
                if (pro.getExecutionTime() <= 0) {
                    auxListProcess.add(pro);
                    partition.setAssignedProcess(null);
                }
            }
        }
        return auxListProcess;
    }

    public void condensate(int index) {
        for (int i = index; i < currentPartitionList.size() - 1; i++) {
            if (currentPartitionList.get(i).getAssignedProcess() == null && currentPartitionList.get(i + 1).getAssignedProcess() == null) {
                Partition par1 = currentPartitionList.get(i);
                Partition par2 = currentPartitionList.get(i + 1);
                Condensation con = new Condensation(currentPartitionList.get(i), currentPartitionList.get(i + 1));
                Partition conPar = new Partition(con.getCondensationSize());
                con.setCondensatedPartition(conPar);
                condensations.add(con);
                currentPartitionList.set(i, conPar);
                currentPartitionList.remove(i + 1);
                try {
                    partitionsList.add((Partition) conPar.clone());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        if (canCondensate()) {
            condensate(0);
        }
    }

    public int getIndexOfPartitionToAssignProcess(int fromIndex, Process process) {
        for (int i = fromIndex; i < currentPartitionList.size(); i++) {
            Partition par = currentPartitionList.get(i);
            if (par.getAssignedProcess() == null && par.getPartitionSize() > process.getProcessSize()) {
                return i;
            }
        }
        return -1;
    }

    private boolean canCondensate() {
        for (int i = 0; i < currentPartitionList.size() - 1; i++) {
            if (currentPartitionList.get(i).getAssignedProcess() == null
                    && currentPartitionList.get(i + 1).getAssignedProcess() == null) {
                return true;
            }
        }
        return false;
    }

    /**
     * Busca el proceso con el nombre especificado dentro de la lista
     * especifica- da
     *
     * @param name Nombre del proceso
     * @param list Lista en la cual debe buscar el proceso
     * @return El proceso con el nombre especificado, null si no lo encontró
     */
    public Process searchProcess(String name, ArrayList<Process> list) throws Exception {
        for (Process process : list) {
            if (process.getName().equals(name)) {
                return process;
            }
        }
        throw new Exception("No se pudo encontrar el proceso: " + name + ", en la lista: " + list.toString());
    }

    /**
     * Busca el proceso con el nombre especificado dentro de la lista
     * especifica- da
     *
     * @param name Nombre del proceso
     * @return El proceso con el nombre especificado, null si no lo encontró
     */
    public Partition searchPartition(String name) throws Exception {
        for (Partition partition : partitionsList) {
            if (partition.getPartitionName().equals(name)) {
                return partition;
            }
        }
        throw new Exception("No se pudo encontrar la partición: " + name);
    }

    public Object[] getPartitionTableHeaders() {
        ArrayList<String> partitionsHeaders = new ArrayList<>();
        for (Partition partition : partitionsList) {
            partitionsHeaders.add("Part. " + partition.getPartitionName());
        }

        return partitionsHeaders.toArray();
    }

    //---------------- Getters & Setters -----------------------
    public ArrayList<Process> getInput_ProcessList() {
        return input_ProcessList;
    }

    public ArrayList<Process> getOutput_ProcessList() {
        return output_ProcessList;
    }

    public double getQuantum() {
        return quantum;
    }

    public ArrayList<Partition> getPartitionsList() {
        return partitionsList;
    }

    public ArrayList<Partition> getOutput_PartitionsList() {
        return output_PartitionList;
    }

    public ArrayList<Condensation> getCondensations() {
        return condensations;
    }

    @Override
    public String toString() {
        String aux = "Partitions general\n";
        for (Partition partition : partitionsList) {
            aux += partition.toString();
        }
        aux += "\ncondenzazaos\n";
        for (Condensation con : condensations) {
            aux += con.toString();
        }
        aux += "\nlista salida procesos\n";
        for (Process process : output_ProcessList) {
            aux += process.toString();
        }
        aux += "\nlista salida particiones\n";
        for (Partition par : output_PartitionList) {
            aux += par.toString();
        }
        return aux;
    }

    public void setQuantum(int quantum) {
        this.quantum = quantum;
    }

    public void setMemory(int memory) {
        this.memory = memory;
    }

}
