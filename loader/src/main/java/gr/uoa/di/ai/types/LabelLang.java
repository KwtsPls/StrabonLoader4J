package gr.uoa.di.ai.types;

import java.util.Objects;

public class LabelLang {

    String label;
    String lang;

    public LabelLang(String label, String lang) {
        this.label = label;
        this.lang = lang;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getLang() {
        return lang;
    }

    public void setLang(String lang) {
        this.lang = lang;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LabelLang labelLang = (LabelLang) o;
        String s1 = this.label + this.lang;
        String s2 = labelLang.label + labelLang.lang;
        return s1.equals(s2);
    }

    @Override
    public int hashCode() {
        String s = label + lang;
        return Objects.hash(s);
    }
}
