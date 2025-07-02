package com.example.demo.client;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class FxRates {

    @JacksonXmlProperty(localName = "FxRate")
    @JacksonXmlElementWrapper(useWrapping = false)
    private List<FxRate> fxRate;

    public List<FxRate> getFxRate() {
        return fxRate;
    }

    public void setFxRate(List<FxRate> fxRate) {
        this.fxRate = fxRate;
    }

    public static class FxRate {
        @JacksonXmlProperty(localName = "Tp")
        private String tp;

        @JacksonXmlProperty(localName = "Dt")
        private LocalDate dt;

        @JacksonXmlProperty(localName = "CcyAmt")
        @JacksonXmlElementWrapper(useWrapping = false)
        private List<CcyAmt> ccyAmt;

        public String getTp() {
            return tp;
        }

        public void setTp(String tp) {
            this.tp = tp;
        }

        public LocalDate getDt() {
            return dt;
        }

        public void setDt(LocalDate dt) {
            this.dt = dt;
        }

        public List<CcyAmt> getCcyAmt() {
            return ccyAmt;
        }

        public void setCcyAmt(List<CcyAmt> ccyAmt) {
            this.ccyAmt = ccyAmt;
        }
    }

    public static class CcyAmt {
        @JacksonXmlProperty(localName = "Ccy")
        private String ccy;

        @JacksonXmlProperty(localName = "Amt")
        private BigDecimal amt;

        public String getCcy() {
            return ccy;
        }

        public void setCcy(String ccy) {
            this.ccy = ccy;
        }

        public BigDecimal getAmt() {
            return amt;
        }

        public void setAmt(BigDecimal amt) {
            this.amt = amt;
        }
    }
}
