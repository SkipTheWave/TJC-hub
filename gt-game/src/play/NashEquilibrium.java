package play;

public class NashEquilibrium {

    private int nRow;
    private int nCol;
    private double u1;
    private double u2;

    public NashEquilibrium(int nRow, int nCol, double u1, double u2) {
        this.nRow = nRow;
        this.nCol = nCol;
        this.u1 = u1;
        this.u2 = u2;
    }

    public int getnRow() {
        return nRow;
    }

    public void setnRow(int nRow) {
        this.nRow = nRow;
    }

    public int getnCol() {
        return nCol;
    }

    public void setnCol(int nCol) {
        this.nCol = nCol;
    }

    public double getU1() {
        return u1;
    }

    public void setU1(double u1) {
        this.u1 = u1;
    }

    public double getU2() {
        return u2;
    }

    public void setU2(double u2) {
        this.u2 = u2;
    }

    public int IsNashEquilibriumGreaterThan(double u1, double u2) {
        if (this.u1 > u1 && this.u2 > u2) {
            return 1;
        } else {
            return 0;
        }
    }
}
