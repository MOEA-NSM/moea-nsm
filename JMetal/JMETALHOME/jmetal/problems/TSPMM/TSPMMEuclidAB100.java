package jmetal.problems.TSPMM;

import java.io.*;

/**
 * Esta é uma classe que representa uma instancia TSP Bi - euclidA100 and
 * euclidB100 onde todo o trabalho fica por conta da classe pai
 */
public class TSPMMEuclidAB100 extends TSPMMProblema {

    public TSPMMEuclidAB100(String solutionType) throws IOException {
        super("Permutation", "euclidA100", "euclidB100", "TSPMEuclidAB100");
    }
}
