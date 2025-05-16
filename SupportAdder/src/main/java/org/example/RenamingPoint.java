package org.example;

public class RenamingPoint  implements Point{
    private final String oldNum;
    private final String newNum;
    private boolean status;

    public RenamingPoint(String oldNum, String newNum){
        this.oldNum = oldNum;
        this.newNum = newNum;
    }

    public String getNewNum() {
        return newNum;
    }

    public String getOldNum(){
        return oldNum;
    }

    public String getNum(){
        return getOldNum();
    }

    public void setStatus(boolean status) {
        this.status = !status;
    }

    public boolean getStatus() {
        return status;
    }
}
