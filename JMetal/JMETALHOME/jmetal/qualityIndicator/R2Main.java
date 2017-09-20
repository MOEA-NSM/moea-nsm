/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jmetal.qualityIndicator;

/**
 *
 * @author Deyvid
 */
public class R2Main {

    /**
     * Vide Metodo main da class R2
     * This class can be call from the command line. At least three parameters
     * are required: 1) the name of the file containing the front, 2) the number
     * of objectives 2) a file containing the reference point / the Optimal
     * Pareto front for normalizing 3) the file containing the weight vector
     */
    public static void main(String args[]) {

        String pathFronteiraGerada = "E://testeR2//FUN.0";
        String pathParetoFront = "E://testeR2//besteuclidAB100.pf";

        
        calcularR2DoisObjetivos(pathFronteiraGerada, pathParetoFront);

    } // main

    public static void calcularR2DoisObjetivos(String pathFronteiraGerada, String pathParetoFront) {
        //Create a new instance of the metric
        R2 qualityIndicator = new R2();

        //Fronteira gerada pelo metodo para comparacao com a pareto otimo
        double[][] approximationFront = qualityIndicator.utils_.readFront(pathFronteiraGerada);

        //Fronteira de Pareto Otimo
        double[][] paretoFront = qualityIndicator.utils_.readFront(pathParetoFront);

        //Obtain delta value
        double value = qualityIndicator.R2(approximationFront, paretoFront);
//        double value = qualityIndicator.R2(paretoFront, paretoFront);

        System.out.println(value);

        System.out.println(qualityIndicator.R2Withouth(approximationFront, paretoFront, 1));
        System.out.println(qualityIndicator.R2Withouth(approximationFront, paretoFront, 15));
        System.out.println(qualityIndicator.R2Withouth(approximationFront, paretoFront, 25));
        System.out.println(qualityIndicator.R2Withouth(approximationFront, paretoFront, 75));
    }

}
