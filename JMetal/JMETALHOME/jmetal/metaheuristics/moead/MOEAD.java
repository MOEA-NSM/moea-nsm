//  MOEAD.java
//
//  Author:
//       Antonio J. Nebro <antonio@lcc.uma.es>
//       Juan J. Durillo <durillo@lcc.uma.es>
//
//  Copyright (c) 2011 Antonio J. Nebro, Juan J. Durillo
//
//  This program is free software: you can redistribute it and/or modify
//  it under the terms of the GNU Lesser General Public License as published by
//  the Free Software Foundation, either version 3 of the License, or
//  (at your option) any later version.
//
//  This program is distributed in the hope that it will be useful,
//  but WITHOUT ANY WARRANTY; without even the implied warranty of
//  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//  GNU Lesser General Public License for more details.
// 
//  You should have received a copy of the GNU Lesser General Public License
//  along with this program.  If not, see <http://www.gnu.org/licenses/>.
package jmetal.metaheuristics.moead;

import jmetal.core.*;
import jmetal.util.JMException;
import jmetal.util.PseudoRandom;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.StringTokenizer;
import java.util.Vector;
import jmetal.metaheuristics.moeansm.util.TempoExecucao;
import jmetal.qualityIndicator.QualityIndicator;

public class MOEAD extends Algorithm {

    private int populacaoTamanho_;
    /**
     * Stores the population
     */
    private SolutionSet populacao_;
    /**
     * Z vector (ideal point) É o melhor indivíduo encontrado ate o momento -
     * Elitista
     */
    double[] z_;
    /**
     * Lambda vectors - É a matriz de pesos utilizada para identificar a
     * vizinhança
     */
    //Vector<Vector<Double>> lambda_ ; 
    double[][] lambda_;
    /**
     * T: neighbour size - quantidade de vizinhos de cada solucao - Vizinhanca
     */
    int T_;
    /**
     * Neighborhood
     */
    int[][] vizinhanca_;
    /**
     * delta: probability that parent solutions are selected from neighbourhood
     * probabilidade de solucoes pais serem selecionadas de solucoes vizinhas
     */
    double delta_;
    /**
     * nr: maximal number of solutions replaced by each child solution maximo
     * numero de solucos substituidas por cada solucao filha
     */
    int nr_;
    Solution[] indArray_;
    String functionType_;
    int avaliacoes_;
    /**
     * Operators
     */
    Operator crossover_;
    Operator mutacao_;

//    String dataDirectory_;
    /**
     * Constructor
     *
     * @param problem Problem to solve
     */
    public MOEAD(Problem problem) {
        super(problem);

        functionType_ = "_TCHE1";

    } // DMOEA

    public SolutionSet execute() throws JMException, ClassNotFoundException {
        int maxEvaluations;
        TempoExecucao tempoExecucao = new TempoExecucao();
        /*ESTAGNACAO*/
        int estagnacao = 0;
        int MAX_ESTAGNACAO = 200;
        double HV = 0.0;
        int requiredEvaluations = 0;

        QualityIndicator indicators; // QualityIndicator object

        avaliacoes_ = 0;
        maxEvaluations = ((Integer) this.getInputParameter("maxEvaluations")).intValue();
        populacaoTamanho_ = ((Integer) this.getInputParameter("populationSize")).intValue();
        String dataDirectory_ = this.getInputParameter("dataDirectory").toString();
        indicators = (QualityIndicator) getInputParameter("indicators");

        populacao_ = new SolutionSet(populacaoTamanho_);
        indArray_ = new Solution[problem_.getNumberOfObjectives()];

        T_ = ((Integer) this.getInputParameter("T")).intValue();
        nr_ = ((Integer) this.getInputParameter("nr")).intValue();
        delta_ = ((Double) this.getInputParameter("delta")).doubleValue();

        /*
         T_ = (int) (0.1 * populacaoTamanho_);
         delta_ = 0.9;
         nr_ = (int) (0.01 * populacaoTamanho_);
         */
        vizinhanca_ = new int[populacaoTamanho_][T_];

        z_ = new double[problem_.getNumberOfObjectives()];
        //lambda_ = new Vector(problem_.getNumberOfObjectives()) ;
        lambda_ = new double[populacaoTamanho_][problem_.getNumberOfObjectives()];

        crossover_ = operators_.get("crossover"); // default: DE crossover
        mutacao_ = operators_.get("mutation");  // default: polynomial mutation

        // STEP 1. Initialization
        // STEP 1.1. Compute euclidean distances between weight vectors and find T
        inicializarMatrizPesos(dataDirectory_);
        //for (int i = 0; i < 300; i++)
        // 	System.out.println(lambda_[i][0] + " " + lambda_[i][1]) ;

        inicializarVizinhanca();

        // STEP 1.2. Initialize population
        inicializarPopulacao();

        // STEP 1.3. Initialize z_
        inicializarPontoIdealElitista();

        // STEP 2. Update
        do {
            int[] permutation = new int[populacaoTamanho_];
            Utils.randomPermutation(permutation, populacaoTamanho_);
            /*permutation gera uma lista de indices aleatorios que são usados no processo de selecao para aplicacao dos operadores geneticos*/

            for (int i = 0; i < populacaoTamanho_; i++) {
                int r1 = permutation[i]; // or int n = i; 
                //int n = i ; // or int n = i;
                int type;
                double rnd = PseudoRandom.randDouble();

                // STEP 2.1. Mating selection based on probability
                if (rnd < delta_) // if (rnd < realb)    
                {
                    type = 1;   // neighborhood
                } else {
                    type = 2;   // whole population
                }
                Vector<Integer> offspring = new Vector<Integer>();
                selecaoPermutacao(offspring, r1, 2, type);//seleciona 3 individuos, sendo da vizinhanca (type 1) ou da populacao geral e retorna na lista offspring 

                // STEP 3.2. Reproduction
                Solution childY;
                Solution[] parents = new Solution[3];

                parents[0] = populacao_.get(offspring.get(0));
                parents[1] = populacao_.get(offspring.get(1));
                parents[2] = populacao_.get(r1);

                //child = ((Solution[]) crossover_.execute(parents))[0];                
                //Apply DE crossover operator
//                childY = (Solution) crossover_.execute(new Object[]{populacao_.get(r1), parents});
                childY = ((Solution[]) crossover_.execute(parents))[0];
//                child2 = ((Solution[]) crossover_.execute(parents))[1];

                //Apply mutation
                mutacao_.execute(childY);

                // Evaluation
                problem_.evaluate(childY);

                avaliacoes_++;

                // STEP 3.3. Repair. Not necessary
                // STEP 3.4. Update z_ Busca o Elitista e armazena em z_
                atualizarElitista(childY);

                // STEP 3.5. Update of solutions
                atualizarSolucoes(childY, r1, type);
            } // for 

            // This piece of code shows how to use the indicator object into the code
            // of NSGA-II. In particular, it finds the number of evaluations required
            // by the algorithm to obtain a Pareto front with a hypervolume higher
            // than the hypervolume of the true Pareto front.
            if ((indicators != null) && (requiredEvaluations == 0)) {
                double HVAtual = indicators.getHypervolume(populacao_);
                if (HV >= HVAtual && HV != 0.0) {
                    estagnacao++;
                } else {
                    HV = HVAtual;
                    estagnacao = 0;
                }
                if (estagnacao >= MAX_ESTAGNACAO) {
//                    System.out.println("MOEAD Estagnou na geracao: " + avaliacoes_);
                    break;
                }
            } // if

        } while (avaliacoes_ < maxEvaluations);

        populacao_.setTempoExecucao(tempoExecucao.getTempo());
        populacao_.setGeracaoParou(avaliacoes_);

        return populacao_;
    }

    /**
     * inicializarMatrizPesos
     */
    public void inicializarMatrizPesos(String dataDirectory_) {
        if ((problem_.getNumberOfObjectives() == 2) && (populacaoTamanho_ <= 300)) {
            for (int n = 0; n < populacaoTamanho_; n++) {
                double a = 1.0 * n / (populacaoTamanho_ - 1);
                lambda_[n][0] = a;
                lambda_[n][1] = 1 - a;
            } // for
        } // if
        else {
            String dataFileName;
            dataFileName = "W" + problem_.getNumberOfObjectives() + "D_"
                    + populacaoTamanho_ + ".dat";

            try {
                // Open the file
                FileInputStream fis = new FileInputStream(dataDirectory_ + "/" + dataFileName);
                InputStreamReader isr = new InputStreamReader(fis);
                BufferedReader br = new BufferedReader(isr);

                int numberOfObjectives = 0;
                int i = 0;
                int j = 0;
                String aux = br.readLine();
                while (aux != null) {
                    StringTokenizer st = new StringTokenizer(aux);
                    j = 0;
                    numberOfObjectives = st.countTokens();
                    while (st.hasMoreTokens()) {
                        double value = (new Double(st.nextToken())).doubleValue();
                        lambda_[i][j] = value;
                        //System.out.println("lambda["+i+","+j+"] = " + value) ;
                        j++;
                    }
                    aux = br.readLine();
                    i++;
                }
                br.close();
            } catch (Exception e) {
                System.out.println("inicializarMatrizPesos: failed when reading for file: " + dataDirectory_ + "/" + dataFileName);
                e.printStackTrace();
            }
        } // else

        //System.exit(0) ;
    } // inicializarMatrizPesos

    /**
     * Calcula as distancias baseadas nos vetores de peso o vetor pode vir de um
     * arquivo externo caso a população seja maior que 300 individuos ou ser
     * apenas calculado pesos
     */
    public void inicializarVizinhanca() {
        double[] x = new double[populacaoTamanho_];
        int[] idx = new int[populacaoTamanho_];

        for (int i = 0; i < populacaoTamanho_; i++) {
            // calcula a distancia baseada no vetor de pesos - calculate the distances based on weight vectors
            for (int j = 0; j < populacaoTamanho_; j++) {
                x[j] = Utils.distVector(lambda_[i], lambda_[j]);
                //x[j] = dist_vector(population[i].namda,population[j].namda);
                idx[j] = j;
                //System.out.println("x["+j+"]: "+x[j]+ ". idx["+j+"]: "+idx[j]) ;
            } // for

            //Encontra os subproblemas vizinhos mais proximos - find 'niche' nearest neighboring subproblems
            //Ordena toda populacao baseada na distancia euclidiana e depois fica com somente T_solucoes na vizinhanca de cada solucao
            Utils.minFastSort(x, idx, populacaoTamanho_, T_);
            //minfastsort(x,idx,population.size(),niche);
            //Copia para vizinhanca as T_ solucoes mais proximas do da solucao i
            System.arraycopy(idx, 0, vizinhanca_[i], 0, T_);
        } // for
    } // inicializarVizinhanca

    /**
     *
     */
    public void inicializarPopulacao() throws JMException, ClassNotFoundException {
        for (int i = 0; i < populacaoTamanho_; i++) {
            Solution newSolution = new Solution(problem_);

            problem_.evaluate(newSolution);
            avaliacoes_++;
            populacao_.add(newSolution);
        } // for
    } // inicializarPopulacao

    /**
     *
     */
    void inicializarPontoIdealElitista() throws JMException, ClassNotFoundException {
        for (int i = 0; i < problem_.getNumberOfObjectives(); i++) {
            z_[i] = 1.0e+30;
            indArray_[i] = new Solution(problem_);
            problem_.evaluate(indArray_[i]);
            avaliacoes_++;
        } // for

        for (int i = 0; i < populacaoTamanho_; i++) {
            atualizarElitista(populacao_.get(i));
        } // for
    } // inicializarPontoIdeal

    /**
     *
     * @param individual
     */
    void atualizarElitista(Solution individual) {
        for (int n = 0; n < problem_.getNumberOfObjectives(); n++) {
            if (individual.getObjective(n) < z_[n]) {
                z_[n] = individual.getObjective(n);

                indArray_[n] = individual;
            }
        }
    } // atualizarReferencia

    /**
     *
     */
    public void selecaoPermutacao(Vector<Integer> listSelecao, int individuoAleatorio_, int size, int type) {
        // list : the set of the indexes of selected mating parents
        // cid  : the id of current subproblem
        // size : the number of selected mating parents
        // type : 1 - neighborhood; otherwise - whole population
        int ss;
        int r;
        int p;

        ss = vizinhanca_[individuoAleatorio_].length;
        while (listSelecao.size() < size) {

            /*Se o type for igual a 1 é para selecionar individuo da vizinhanca, 
             ou seja, proximo a solucao de referencia, senao seleciona da população total*/
            if (type == 1) {
                r = PseudoRandom.randInt(0, ss - 1);
                p = vizinhanca_[individuoAleatorio_][r];
                //p = population[cid].table[r];
            } else {
                p = PseudoRandom.randInt(0, populacaoTamanho_ - 1);
            }
            boolean flag = true;
            for (int i = 0; i < listSelecao.size(); i++) {
                if (listSelecao.get(i) == p) // p is in the list
                {
                    flag = false;
                    break;
                }
            }

            //if (flag) list.push_back(p);
            if (flag) {
                listSelecao.addElement(p);
            }
        }
    } // selecaoPermutacao

    /**
     * Step 3.5 Update of Solutions
     *
     * @param individual
     * @param r1
     * @param type childY, r1, type
     */
    void atualizarSolucoes(Solution childY, int r1, int type) {
        // indiv: childY solution
        // r1:   the id of current subproblem
        // type: update solutions in - neighborhood (1) or whole population (otherwise)
        int size;
        int timeC;

        timeC = 0;

        if (type == 1) {
            size = vizinhanca_[r1].length;
        } else {
            size = populacao_.size();
        }
        int[] perm = new int[size];

        Utils.randomPermutation(perm, size);

        for (int i = 0; i < size; i++) {
            int k;
            if (type == 1) {
                k = vizinhanca_[r1][perm[i]];
            } else {
                k = perm[i];      // calculate the values of objective function regarding the current subproblem
            }
            double f1, f2;

            f1 = funcaoFitness(populacao_.get(k), lambda_[k]);
            f2 = funcaoFitness(childY, lambda_[k]);

            if (f2 < f1) {
                populacao_.replace(k, new Solution(childY));
                //population[k].indiv = indiv;
                timeC++;
            }
            /*maximo numero de solucoes substituidas por cada solucao filha
             the maximal number of solutions updated is not allowed to exceed 'limit'
             */
            if (timeC >= nr_) {
                return;
            }
        }
    } // atualizarProblema

    double funcaoFitness(Solution individual, double[] lambda) {
        double fitness;
        fitness = 0.0;

        if (functionType_.equals("_TCHE1")) {
            double maxFun = -1.0e+30;
            //FUNCIONAMENTO DO TCHEBYCHEFF
            for (int n = 0; n < problem_.getNumberOfObjectives(); n++) {
                double diff = Math.abs(individual.getObjective(n) - z_[n]);

                double feval;
                if (lambda[n] == 0) {
                    feval = 0.0001 * diff;
                } else {
                    feval = diff * lambda[n];
                }
                if (feval > maxFun) {
                    maxFun = feval;
                }
            } // for

            fitness = maxFun;
        } // if
        else {
            System.out.println("MOEAD.funcaoFitness: unknown type " + functionType_);
            System.exit(-1);
        }
        return fitness;
    } // fitnessEvaluation

    double funcaoFitnessSemLambda(Solution individual) {
        //FUNCIONAMENTO DO TCHEBYCHEFF Sem o lambda
        double fitness = 0.0;
        double maxFun = Double.MAX_VALUE;
        for (int n = 0; n < problem_.getNumberOfObjectives(); n++) {
            double diff = Math.abs(individual.getObjective(n) - z_[n]);
            if (diff < maxFun) {
                maxFun = diff;
            }
        } // for
        fitness = maxFun;
        return fitness;
    }
} // MOEAD

