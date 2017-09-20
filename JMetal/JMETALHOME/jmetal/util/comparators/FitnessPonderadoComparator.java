package jmetal.util.comparators;

import java.util.Comparator;
import jmetal.core.Solution;

/**
 *
 * @author Deyvid
 * @date 28/01/2016
 */
/**
 * This class implements a <code>Comparator</code> um metodo para comparar as
 * solucoes <code>Solution</code> baseado na soma dos objetivos objetivos
 * (ponderada) retornado pelo metodo <code>getFitness</code>.
 *
 */
public class FitnessPonderadoComparator implements Comparator {

    private boolean ascendingOrder_;

    public FitnessPonderadoComparator(boolean ascendingOrder_) {
        this.ascendingOrder_ = ascendingOrder_;
    }

    /**
     * Compara duas solucoes.
     *
     * @param o1 Objeto representando a primeira <code>Solution</code>.
     * @param o2 Objeto representando a segunda <code>Solution</code>.
     * @return -1, ou 0, ou 1 se o1 é menor doque, igual, or maior doque o2,
     * repectivamente.
     */
    public int compare(Object o1, Object o2) {
        if (o1 == null) {
            return 1;
        } else if (o2 == null) {
            return -1;
        }

        double somaSolucao1 = calcularSomaPorObjetivo(o1);
        double somaSolucao2 = calcularSomaPorObjetivo(o2);

        if (ascendingOrder_) {
            if (somaSolucao1 < somaSolucao2) {
                return -1;
            } else if (somaSolucao1 > somaSolucao2) {
                return 1;
            } else {
                return 0;
            }
        } else {
            if (somaSolucao1 < somaSolucao2) {
                return 1;
            } else if (somaSolucao1 > somaSolucao2) {
                return -1;
            } else {
                return 0;
            }
        }
    } // compare    

    private double calcularSomaPorObjetivo(Object o1) {
        double somaSolucao1 = 0.0;
        for (int i = 0; i < ((Solution) o1).getNumberOfObjectives(); i++) {
            somaSolucao1 += ((Solution) o1).getObjective(i);
        }
        return somaSolucao1;
    }
} // FitnessComparator
