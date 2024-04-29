package gr.uoa.di.ai.types;

import java.util.Objects;

public class LabelDatatype {
    String label;
    String datatype;

    public LabelDatatype(String label, String datatype){
        this.label = label;
        this.datatype = datatype;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getDatatype() {
        return datatype;
    }

    public void setDatatype(String datatype) {
        this.datatype = datatype;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LabelDatatype that = (LabelDatatype) o;
        String s1 = this.label + this.datatype;
        String s2 = that.label + that.datatype;
        return s1.equals(s2);
    }

    @Override
    public int hashCode() {
        String s = this.label + this.datatype;
        return Objects.hash(s);
    }
}
