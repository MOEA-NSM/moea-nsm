package jmetal.problems.TSPMM;

import jmetal.core.Problem;
import jmetal.core.Solution;
import jmetal.encodings.solutionType.PermutationSolutionType;
import jmetal.encodings.variable.Permutation;

import java.io.*;
import jmetal.metaheuristics.moeansm.util.UtilAP;

/**
 * Class representing a multi-objective TSP (Traveling Salesman Problem)
 * problem. This class is tested with two objectives and the KROA150 and KROB150
 * instances of TSPLIB
 */
public class TSPMMProblema extends Problem {

    public int numberOfCities_;
    public double[][] distanceMatrix_;
    public double[][] costMatrix_;
    


//   public MTSP(String solutionType) throws ClassNotFoundException, IOException {
//    this(solutionType, "E://TSPBiobjetivo//kroA100.tsp", "E://TSPBiobjetivo//kroB100.tsp") ;
//  }  
    /**
     * Creates a new mTSP problem instance. It accepts data files from TSPLIB
     */
    
    
    
    public TSPMMProblema() {
    }

    public TSPMMProblema(String solutionType, String instancia1, String instancia2, String nomeProblema) throws IOException {
        numberOfVariables_ = 1;
        numberOfObjectives_ = 2;
        numberOfConstraints_ = 0;
       
        this.problemName_ = nomeProblema;
        this.instancia1 = instancia1;
        this.instancia2 = instancia2;

        //variableType_ = new Class[numberOfVariables_] ;
        length_ = new int[numberOfVariables_];

        //variableType_[0] = Class.forName("jmetal.base.encodings.variable.Permutation") ;
        distanceMatrix_ = readProblem(UtilAP.obterDiretorioProjetoInstancias(this.instancia1));
        costMatrix_ = readProblem(UtilAP.obterDiretorioProjetoInstancias(this.instancia2));
        System.out.println(numberOfCities_);
        length_[0] = numberOfCities_;
        if (solutionType.compareTo("Permutation") == 0) {
            solutionType_ = new PermutationSolutionType(this);
        } else {
            System.out.println("Error: solution type " + solutionType + " invalid");
            System.exit(-1);
        }
    } // mTSP

    /**
     * CALCULAR FITNESS Evaluates a solution
     *
     * @param solution The solution to evaluate Realiza o calculo do Fitness
     */
    public void evaluate(Solution solution) {
        double fitness1;
        double fitness2;

        fitness1 = 0.0;
        fitness2 = 0.0;

        for (int i = 0; i < (numberOfCities_ - 1); i++) {
            int x;
            int y;

            x = ((Permutation) solution.getDecisionVariables()[0]).vector_[i];
            y = ((Permutation) solution.getDecisionVariables()[0]).vector_[i + 1];
//  cout << "I : " << i << ", x = " << x << ", y = " << y << endl ;    
            fitness1 += distanceMatrix_[x][y];
            fitness2 += costMatrix_[x][y];
        } // for
        int firstCity;
        int lastCity;

        firstCity = ((Permutation) solution.getDecisionVariables()[0]).vector_[0];
        lastCity = ((Permutation) solution.getDecisionVariables()[0]).vector_[numberOfCities_ - 1];
        fitness1 += distanceMatrix_[firstCity][lastCity];
        fitness2 += costMatrix_[firstCity][lastCity];

        solution.setObjective(0, fitness1);
        solution.setObjective(1, fitness2);
    } // evaluate

    @Override
    public double obterDistanciaObj1(int x, int y) {
        return distanceMatrix_[x][y];
    }

    @Override
    public double obterCustoObj2(int x, int y) {
        return costMatrix_[x][y];
    }

    @Override
    public double obterDistanciaPonderada(int x, int y) {
        return obterDistanciaObj1(x, y) + obterCustoObj2(x, y);
    }

    @Override
    public double obterDistanciaPorObjetivo(int objetivo, int x, int y) {
        if (objetivo == 0) {
            return obterDistanciaObj1(x, y);
        } else if (objetivo == 1) {
            return obterCustoObj2(x, y);
        }
        return 0.0;

    }

    public double[][] readProblem(String file) throws IOException {
        double[][] matrix = null;
        Reader inputFile = new BufferedReader(new InputStreamReader(new FileInputStream(file)));

        StreamTokenizer token = new StreamTokenizer(inputFile);
        try {
            boolean found;
            found = false;

            token.nextToken();
            while (!found) {
                if ((token.sval != null) && ((token.sval.compareTo("DIMENSION") == 0))) {
                    found = true;
                } else {
                    token.nextToken();
                }
            } // while

            token.nextToken();
            token.nextToken();

            numberOfCities_ = (int) token.nval;

            matrix = new double[numberOfCities_][numberOfCities_];

            // Find the string SECTION  
            found = false;
            token.nextToken();
            while (!found) {
                if ((token.sval != null)
                        && ((token.sval.compareTo("SECTION") == 0))) {
                    found = true;
                } else {
                    token.nextToken();
                }
            } // while

            // Read the data
            double[] c = new double[2 * numberOfCities_];

            for (int i = 0; i < numberOfCities_; i++) {
                token.nextToken();
                int j = (int) token.nval;

                token.nextToken();
                c[2 * (j - 1)] = token.nval;
                token.nextToken();
                c[2 * (j - 1) + 1] = token.nval;
            } // for

            double dist;
            for (int k = 0; k < numberOfCities_; k++) {
                matrix[k][k] = 0;
                for (int j = k + 1; j < numberOfCities_; j++) {
                    dist = Math.sqrt(Math.pow((c[k * 2] - c[j * 2]), 2.0)
                            + Math.pow((c[k * 2 + 1] - c[j * 2 + 1]), 2));
                    dist = (int) (dist + .5);
                    matrix[k][j] = dist;
                    matrix[j][k] = dist;
                } // for
            } // for
        } // try
        catch (Exception e) {
            System.err.println("TSP.readProblem(): error when reading data file " + e);
            System.exit(1);
        } // catch
        return matrix;
    } // readProblem
} // mTSP
