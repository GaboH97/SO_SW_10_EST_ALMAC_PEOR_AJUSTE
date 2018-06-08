package logic;

import java.util.ArrayList;

/**
 *
 * @author Gabriel Amaya, Cesar Cardozo
 */
public class Partition implements Cloneable{

    //------------------ Attributes --------------------------
    private String partitionName;
    private int partitionID;
    private int partitionSize;
    //MIRAR SI ES UNA LISTA O UN SOLO PROCESO
    //IGUAL SOLO SE EJECUTA UNO Y LUEGO SE LIBERA LA PARTICIÓN
    private Process assignedProcess;
    public static int BASE_ID = 1;

    //--------------------- Constructors ------------------------
    /**
     *  CUANDO HAY UNA CONDENSACIÓN, SE LE PASA EL TAMAÑO DE LA NUEVA PARTICIÓN
     * 
     * @param partitionSize
     */
    public Partition(int partitionSizde) {
        partitionID = BASE_ID++;
        setPartitionName("PA" + String.valueOf(partitionID));
        this.partitionSize = partitionSizde;
        this.assignedProcess = null;
    }

    /**
     * CUANDO ES HUECO SE LE PONE NULL
     *
     * @param assignedProcess
     */
    public Partition(Process assignedProcess) {
        partitionID = BASE_ID++;
        setPartitionName("PA" + String.valueOf(partitionID));
        this.assignedProcess = assignedProcess;
        this.partitionSize = this.assignedProcess.getProcessSize();   
    }

    public Partition(String partitionName, int partitionID, int partitionSize, Process assignedProcess) {
        this.partitionName = partitionName;
        this.partitionID = partitionID;
        this.partitionSize = partitionSize;
        this.assignedProcess = assignedProcess;
    }

    public int getTotalExecutionTime() {
        return assignedProcess.getExecutionTime();
    }

    //------------------ Getters & Setters --------------------------
    public Process getAssignedProcess() {
        return assignedProcess;
    }

    public void setAssignedProcess(Process assignedProcess) {
        this.assignedProcess = assignedProcess;
    }

    public String getPartitionName() {
        return partitionName;
    }

    public int getPartitionID() {
        return partitionID;
    }

    public void setPartitionName(String partitionName) {
        this.partitionName = partitionName;
    }

    public int getPartitionSize() {
        return partitionSize;
    }

    public void setPartitionSize(int partitionSize) {
        this.partitionSize = partitionSize;
    }

    @Override
    public String toString() {
        return this.partitionName + "Partition " + this.assignedProcess + "size" + this.partitionSize + "\n";
    }
    
    @Override
    protected Object clone() throws CloneNotSupportedException {
        return new Partition(this.partitionName, this.partitionID, this.partitionSize, this.assignedProcess);
    }   
    
    public Object[] getDataVectorForTableOfProcessesThatPassed(){
        return new Object[]{getPartitionName(),(getAssignedProcess()!=null)?getAssignedProcess().getName():"NA",getPartitionSize()};
    }
}