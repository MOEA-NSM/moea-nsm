package jmetal.metaheuristics.abordagem.util;

import java.util.Date;

/**
 * TempoExecucao TempoExecucao = new TempoExecucao();
 * metodos
 * System.out.println( TempoExecucao ); 
 */
public class TempoExecucao {

    private Date start;

    public TempoExecucao() {
        reset();
    }

    public long getTempo() {
        Date now = new Date();
        long milisegundos = now.getTime() - start.getTime();

        return milisegundos;
    }

    public void reset() {
        start = new Date(); // now
    }

    
    
    public static String toString(boolean mili, long milisegundos) {
//        long milisegundos = getTempo();

        long hora = milisegundos / 1000 / 60 / 60;
        milisegundos -= hora * 1000 * 60 * 60;

        long minuto = milisegundos / 1000 / 60;
        milisegundos -= minuto * 1000 * 60;

        long seconds = milisegundos / 1000;
        milisegundos -= seconds * 1000;

        StringBuffer tempo = new StringBuffer();
        if (hora > 0) {
            tempo.append(hora + ":");
        }
        if (hora > 0 && minuto < 10) {
            tempo.append("0");
        }
        tempo.append(minuto + ":");
        if (seconds < 10) {
            tempo.append("0");
        }
        tempo.append(seconds);

        if (mili) {
            tempo.append(".");
            if (milisegundos < 100) {
                tempo.append("0");
            }
            if (milisegundos < 10) {
                tempo.append("0");
            }
            tempo.append(milisegundos);
        }

        return tempo.toString();
    }

    @Override
    public String toString() {
        return toString(true, this.getTempo());
    }

    public static void main(String[] args) {
        TempoExecucao tempoExecucao = new TempoExecucao();

        for (int i = 0; i < 100000000; i++) {
            double b = 998.43678;
            double c = Math.sqrt(b);
        }

        System.out.println(tempoExecucao);
    }
}
