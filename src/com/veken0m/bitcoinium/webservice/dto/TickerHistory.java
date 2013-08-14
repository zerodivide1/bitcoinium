
package com.veken0m.xhub.dto;

import java.math.BigDecimal;
import java.util.ArrayList;

public class TickerHistory {
    private ArrayList<BigDecimal> pp;
    private BigDecimal t;
    private ArrayList<BigDecimal> tt;

    public ArrayList<BigDecimal> getPp() {
        return this.pp;
    }

    public void setPp(ArrayList<BigDecimal> pp) {
        this.pp = pp;
    }

    public BigDecimal getT() {
        return this.t;
    }

    public void setT(BigDecimal t) {
        this.t = t;
    }

    public ArrayList<BigDecimal> getTt() {
        return this.tt;
    }

    public void setTt(ArrayList<BigDecimal> tt) {
        this.tt = tt;
    }
}
