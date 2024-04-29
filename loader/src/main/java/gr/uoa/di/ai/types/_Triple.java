package gr.uoa.di.ai.types;

public class _Triple {
    int ctx;
    int subj;
    int pred;
    int obj;
    boolean expl;

    public _Triple(int ctx, int subj, int pred, int obj, boolean expl){
        this.ctx = ctx;
        this.subj = subj;
        this.pred = pred;
        this.obj = obj;
        this.expl = expl;
    }

    public int getCtx() {
        return ctx;
    }

    public void setCtx(int ctx) {
        this.ctx = ctx;
    }

    public int getSubj() {
        return subj;
    }

    public void setSubj(int subj) {
        this.subj = subj;
    }

    public int getPred() {
        return pred;
    }

    public void setPred(int pred) {
        this.pred = pred;
    }

    public int getObj() {
        return obj;
    }

    public void setObj(int obj) {
        this.obj = obj;
    }

    public boolean isExpl() {
        return expl;
    }

    public void setExpl(boolean expl) {
        this.expl = expl;
    }
}
