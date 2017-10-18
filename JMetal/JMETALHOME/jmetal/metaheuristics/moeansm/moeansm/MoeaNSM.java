package jmetal.metaheuristics.moeansm.moeansm;

import java.util.Comparator;
import jmetal.core.Algorithm;
import jmetal.core.Operator;
import jmetal.core.Problem;
import jmetal.core.Solution;
import jmetal.core.SolutionSet;
import jmetal.metaheuristics.moeansm.util.TempoExecucao;
import jmetal.metaheuristics.moeansm.util.UtilAP;
import jmetal.qualityIndicator.QualityIndicator;
import jmetal.util.Distance;
import jmetal.util.JMException;
import jmetal.util.Ranking;
import jmetal.util.Spea2Fitness;
import jmetal.util.comparators.CrowdingComparator;
import jmetal.util.comparators.FitnessPonderadoComparator;
import jmetal.util.comparators.ObjectiveComparator;

/**
 * @author Deyvid
 * @date 26/01/2016
 */
public class MoeaNSM extends Algorithm {

    Comparator comparatorObj1_;
    Comparator comparatorObj2_;
    Comparator comparatorPonderada_;
    public boolean aplicarNSGA;
    public boolean aplicarSPEA;
    public boolean aplicarMOEAD;
    /*Z vector (ideal point) É o melhor indivíduo encontrado ate o momento -Elitista */
    double[] z_;
    Solution indArray_;
    double[][] lambda_;
    /**
     * nr: maximal number of solutions replaced by each child solution maximo
     * numero de solucos substituidas por cada solucao filha
     */
    int nr_ = 1; //Deve ser zero

    public MoeaNSM(Problem problema) {
        super(problema);
    }

    /**
     * Executa o algoritmo proposto.
     *
     * @return
     * @throws jmetal.util.JMException
     * @throws java.lang.ClassNotFoundException
     * @returna uma lista de solucoes armazenada em um <code>SolutionSet</code>
     * Retorna um conjunto de solucoes nao dominadas
     */
    @Override
    public SolutionSet execute() throws JMException, ClassNotFoundException {
        int populacaoQtdPontas, populacaoQtdPonderada, populacaoQtdNSGAII, populacaoQtdSPEA2, populacaoTchebyCheff, maxGeracoes, geracao;
        Operator crossoverOperador, mutacaoOperador, selecaoOperador, buscaLocal2OPT;
        SolutionSet pObj1,// Minimizacao do objetivo 2
                pObj2,// Minimizacao do objetivo 1
                pPond,// Ponderada
                pN, // NSGAII
                pS, // SPEA2
                filhosSolucaoSet,
                populacaoUniao,
                pTchebycheff;

        TempoExecucao tempoExecucao = new TempoExecucao();

        QualityIndicator indicadores; // QualityIndicator object
        int requiredEvaluations; // Use in the example of use of the

        /*ESTAGNACAO*/
        int estagnacao = 0;
        int MAX_ESTAGNACAO = 200;
        double HV = 0.0;
        z_ = new double[problem_.getNumberOfObjectives()];
        z_[0] = Double.MAX_VALUE;
        z_[1] = Double.MAX_VALUE;

        //Leitura dos parametros
        populacaoQtdPontas = ((Integer) getInputParameter("populacaoQtdPontas"));
        populacaoQtdPonderada = ((Integer) getInputParameter("populacaoQtdPonderada"));
        populacaoQtdNSGAII = ((Integer) getInputParameter("populacaoQtdNSGAII"));
        populacaoQtdSPEA2 = ((Integer) getInputParameter("populacaoQtdSPEA2"));
        populacaoTchebyCheff = ((Integer) getInputParameter("populacaoTchebyCheff"));
        indicadores = (QualityIndicator) getInputParameter("indicators");

        lambda_ = new double[populacaoTchebyCheff][problem_.getNumberOfObjectives()];
        inicializarMatrizPesos(populacaoTchebyCheff);

        if (populacaoQtdNSGAII > 0) {
            aplicarNSGA = true;
        }
        if (populacaoQtdSPEA2 > 0) {
            aplicarSPEA = true;
        }
        if (populacaoTchebyCheff > 0) {
            aplicarMOEAD = true;
        }

        maxGeracoes = ((Integer) getInputParameter("maxGeracoes"));

        //Leitura dos operadores
        crossoverOperador = operators_.get("crossover");
        mutacaoOperador = operators_.get("mutacao");
        buscaLocal2OPT = operators_.get("buscaLocal2OPT");
        selecaoOperador = operators_.get("selecao");

        //Leitura das instancias
        //Inicializa as variáveis
        pObj1 = new SolutionSet(populacaoQtdPontas);
        pObj2 = new SolutionSet(populacaoQtdPontas);
        pPond = new SolutionSet(populacaoQtdPonderada);
        pN = new SolutionSet(populacaoQtdNSGAII);
        pS = new SolutionSet(populacaoQtdSPEA2);
        pTchebycheff = new SolutionSet(populacaoTchebyCheff);
        /*Para calculo da distancia do NSGAII*/
        Distance distance = new Distance();

        geracao = 0;
        requiredEvaluations = 0;
        /*Para aplicar busca local em toda populacao inicial e depois volta ao normal*/
        Double porcentagemBuscaLocal = (Double) buscaLocal2OPT.getParameter("probability");
        buscaLocal2OPT.setParameter("probability", 1);
        //-> Cria a população inicial aleatoria para Obj1 e Obj2 - solucaoSet
        Solution novaSolucao;
        for (int i = 0; i < populacaoQtdPontas; i++) {
            /*Nova solucao para Obj1*/
            novaSolucao = new Solution(problem_);
            novaSolucao.setObjetivoBuscaLocal(0);
            novaSolucao.setInstancia(problem_.instancia1);
            novaSolucao = (Solution) buscaLocal2OPT.execute(novaSolucao);
            problem_.evaluate(novaSolucao);
            atualizarElitista(novaSolucao);
            pObj1.add(novaSolucao);
            geracao++;
            /*Nova solucao para Obj2*/
            novaSolucao = new Solution(problem_);
            novaSolucao.setObjetivoBuscaLocal(1);
            novaSolucao.setInstancia(problem_.instancia2);
            novaSolucao = (Solution) buscaLocal2OPT.execute(novaSolucao);
            problem_.evaluate(novaSolucao);
            atualizarElitista(novaSolucao);
            pObj2.add(novaSolucao);

            geracao++;
        }

        for (int i = 0; i < populacaoQtdPonderada; i++) {
            /*Nova solucao para pPond*/
            novaSolucao = new Solution(problem_);
            novaSolucao.setObjetivoBuscaLocal(null);
            novaSolucao = (Solution) buscaLocal2OPT.execute(novaSolucao);
            problem_.evaluate(novaSolucao);
            atualizarElitista(novaSolucao);
            pPond.add(novaSolucao);
            geracao++;
        }

        for (int i = 0; i < populacaoTchebyCheff; i++) {
            /*Nova solucao para pPond*/
            novaSolucao = new Solution(problem_);
            novaSolucao.setObjetivoBuscaLocal(null);
            novaSolucao = (Solution) buscaLocal2OPT.execute(novaSolucao);
            problem_.evaluate(novaSolucao);
            atualizarElitista(novaSolucao);
            pTchebycheff.add(novaSolucao);
            geracao++;
        }

        buscaLocal2OPT.setParameter("probability", porcentagemBuscaLocal);

        /*Instancia os comparadores*/
        comparatorObj1_ = new ObjectiveComparator(0);
        comparatorObj2_ = new ObjectiveComparator(1);
        comparatorPonderada_ = new FitnessPonderadoComparator(true);

        /*Aqui comeca a brincadeira*/
        while (geracao < maxGeracoes) {
            /**
             * *******************Operadores geneticos************************
             */
            filhosSolucaoSet = new SolutionSet((populacaoQtdPontas * 2) + populacaoQtdPonderada + populacaoQtdNSGAII + populacaoQtdSPEA2 + (populacaoTchebyCheff * 5));
            Solution[] parents = new Solution[2];
            for (int i = 0; i < (populacaoQtdPontas / 2); i++) {
                if (geracao < maxGeracoes) {
                    //obtain parents
                    parents[0] = (Solution) selecaoOperador.execute(pObj1);
                    parents[1] = (Solution) selecaoOperador.execute(pObj2);
                    Solution[] offSpring = (Solution[]) crossoverOperador.execute(parents);
                    offSpring[0].setObjetivoBuscaLocal(0);
                    offSpring[1].setObjetivoBuscaLocal(0);
                    offSpring[0].setInstancia(problem_.instancia1);
                    offSpring[1].setInstancia(problem_.instancia1);
                    offSpring[0] = (Solution) buscaLocal2OPT.execute(offSpring[0]);
                    offSpring[1] = (Solution) buscaLocal2OPT.execute(offSpring[1]);
                    offSpring[0].setObjetivoBuscaLocal(null);
                    offSpring[1].setObjetivoBuscaLocal(null);
                    mutacaoOperador.execute(offSpring[0]);
                    mutacaoOperador.execute(offSpring[1]);
                    problem_.evaluate(offSpring[0]);
                    problem_.evaluate(offSpring[1]);
                    atualizarElitista(offSpring[0]);
                    atualizarElitista(offSpring[1]);
                    geracao += 2;
                    filhosSolucaoSet.add(offSpring[0]);
                    filhosSolucaoSet.add(offSpring[1]);

                    parents[0] = (Solution) selecaoOperador.execute(pObj1);
                    parents[1] = (Solution) selecaoOperador.execute(pObj2);
                    offSpring = (Solution[]) crossoverOperador.execute(parents);
                    offSpring[0].setObjetivoBuscaLocal(1);
                    offSpring[1].setObjetivoBuscaLocal(1);
                    offSpring[0].setInstancia(problem_.instancia2);
                    offSpring[1].setInstancia(problem_.instancia2);
                    offSpring[0] = (Solution) buscaLocal2OPT.execute(offSpring[0]);
                    offSpring[1] = (Solution) buscaLocal2OPT.execute(offSpring[1]);
                    offSpring[0].setObjetivoBuscaLocal(null);
                    offSpring[1].setObjetivoBuscaLocal(null);
                    mutacaoOperador.execute(offSpring[0]);
                    mutacaoOperador.execute(offSpring[1]);
                    problem_.evaluate(offSpring[0]);
                    problem_.evaluate(offSpring[1]);
                    atualizarElitista(offSpring[0]);
                    atualizarElitista(offSpring[1]);
                    geracao += 2;
                    filhosSolucaoSet.add(offSpring[0]);
                    filhosSolucaoSet.add(offSpring[1]);
                }
            }
            for (int i = 0; i < (populacaoQtdPonderada / 2); i++) {
                if (geracao < maxGeracoes) {
                    //obtain parents
                    parents[0] = (Solution) selecaoOperador.execute(pObj1);
                    parents[1] = (Solution) selecaoOperador.execute(pPond);
                    Solution[] offSpring = (Solution[]) crossoverOperador.execute(parents);
                    offSpring[0] = (Solution) buscaLocal2OPT.execute(offSpring[0]);
                    offSpring[1] = (Solution) buscaLocal2OPT.execute(offSpring[1]);
                    mutacaoOperador.execute(offSpring[0]);
                    mutacaoOperador.execute(offSpring[1]);
                    problem_.evaluate(offSpring[0]);
                    problem_.evaluate(offSpring[1]);
                    atualizarElitista(offSpring[0]);
                    atualizarElitista(offSpring[1]);
                    geracao += 2;
                    filhosSolucaoSet.add(offSpring[0]);
                    filhosSolucaoSet.add(offSpring[1]);
                    parents[0] = (Solution) selecaoOperador.execute(pObj2);
                    parents[1] = (Solution) selecaoOperador.execute(pPond);
                    offSpring = (Solution[]) crossoverOperador.execute(parents);
                    offSpring[0] = (Solution) buscaLocal2OPT.execute(offSpring[0]);
                    offSpring[1] = (Solution) buscaLocal2OPT.execute(offSpring[1]);
                    mutacaoOperador.execute(offSpring[0]);
                    mutacaoOperador.execute(offSpring[1]);
                    problem_.evaluate(offSpring[0]);
                    problem_.evaluate(offSpring[1]);
                    geracao += 2;
                    filhosSolucaoSet.add(offSpring[0]);
                    filhosSolucaoSet.add(offSpring[1]);
                    atualizarElitista(offSpring[0]);
                    atualizarElitista(offSpring[1]);
                }
            }

            if (pS.size() > 0) {
                for (int i = 0; i < (populacaoQtdSPEA2 / 2); i++) {

                    if (geracao < maxGeracoes) {
                        parents[0] = (Solution) selecaoOperador.execute(pS);
                        parents[1] = (Solution) selecaoOperador.execute(pS);
                        Solution[] offSpring = (Solution[]) crossoverOperador.execute(parents);
                        aplicarBuscaLocalAleatorio(offSpring, buscaLocal2OPT);
                        mutacaoOperador.execute(offSpring[0]);
                        mutacaoOperador.execute(offSpring[1]);
                        problem_.evaluate(offSpring[0]);
                        problem_.evaluate(offSpring[1]);
                        geracao += 2;
                        filhosSolucaoSet.add(offSpring[0]);
                        filhosSolucaoSet.add(offSpring[1]);
                        atualizarElitista(offSpring[0]);
                        atualizarElitista(offSpring[1]);
                    }
                }
            }
            if (pN.size() > 0) {
                for (int i = 0; i < (populacaoQtdNSGAII / 2); i++) {
                    if (geracao < maxGeracoes) {
                        parents[0] = (Solution) selecaoOperador.execute(pN);
                        parents[1] = (Solution) selecaoOperador.execute(pN);
                        Solution[] offSpring = (Solution[]) crossoverOperador.execute(parents);
                        aplicarBuscaLocalAleatorio(offSpring, buscaLocal2OPT);
                        mutacaoOperador.execute(offSpring[0]);
                        mutacaoOperador.execute(offSpring[1]);
                        problem_.evaluate(offSpring[0]);
                        problem_.evaluate(offSpring[1]);
                        geracao += 2;
                        filhosSolucaoSet.add(offSpring[0]);
                        filhosSolucaoSet.add(offSpring[1]);
                        atualizarElitista(offSpring[0]);
                        atualizarElitista(offSpring[1]);
                    }
                } // if                            
            } // for

            //Tchebycheff
            if (aplicarMOEAD) {
                for (int i = 0; i < populacaoTchebyCheff; i++) {

                    parents[0] = (Solution) selecaoOperador.execute(pTchebycheff);
                    parents[1] = (Solution) selecaoOperador.execute(pTchebycheff);
                    Solution child, child2;
                    child = ((Solution[]) crossoverOperador.execute(parents))[0];
                    child2 = ((Solution[]) crossoverOperador.execute(parents))[1];

                    int objetivoSelecionado = UtilAP.obterNumeroAleatorio(0, 2);
                    child.setObjetivoBuscaLocal(objetivoSelecionado);
                    child.setInstancia((objetivoSelecionado == 0) ? problem_.instancia1 : problem_.instancia2);
                    child = (Solution) buscaLocal2OPT.execute(child);

                    objetivoSelecionado = UtilAP.obterNumeroAleatorio(0, 2);
                    child2.setObjetivoBuscaLocal(objetivoSelecionado);
                    child2.setInstancia((objetivoSelecionado == 0) ? problem_.instancia1 : problem_.instancia2);
                    child2 = (Solution) buscaLocal2OPT.execute(child2);

                    problem_.evaluate(child);
                    problem_.evaluate(child2);

                    geracao += 2;

                    atualizarElitista(child);
                    atualizarElitista(child2);

                    atualizarSolucoes(child, pTchebycheff);
                    atualizarSolucoes(child2, pTchebycheff);
                }
            }

            filhosSolucaoSet = ((SolutionSet) filhosSolucaoSet).union(pPond).union(pObj1).union(pObj2).union(pTchebycheff);
            /**
             * *************************SPEA2*******************************
             * Junta a populacao atual com a populacao do arquivo respeitando o
             * limite de individuos Calcula o strength e o raw = fitness de cada
             * individuo
             */
            if (aplicarSPEA) {
                Spea2Fitness spea = new Spea2Fitness(filhosSolucaoSet.union(pS));
                spea.fitnessAssign();
                /*Realiza o truncamento*/
                pS = spea.environmentalSelection(populacaoQtdSPEA2);
            }
            /**
             * **********************FIM SPEA2*******************************
             */

            /**
             * **********************NSGAII*******************************
             */
            // Realiza o ranking das 3 populacoes
            if (aplicarNSGA) {
                Ranking ranking = new Ranking(filhosSolucaoSet.union(pN));
                /* remain é responsavel por garantir que o tamanho da populacao nao 
                 ultrapasse a quantidade determinada */
                int remain = populacaoQtdNSGAII;
                int index = 0;
                SolutionSet front = null;
                pN.clear();

                // Obtem a primeira fronteira de solucoes nao dominadas
                front = ranking.getSubfront(index);
                /*Adiciona o maximo de fronteiras possivel. Quando nao couber mais 
                 uma fronteira inteira, sai do while e cai no if abaixo para aplicar 
                 a distancia de multidao e assim pegar somente as solucoes mais
                 bem explalhadas preservando a diversidade nas ultimas vagas restantes*/
                while ((remain > 0) && (remain >= front.size())) {
                    //Assign crowding distance to individuals
//                distance.crowdingDistanceAssignment(front, problem_.getNumberOfObjectives());
                    //Add the individuals of this front
                    for (int k = 0; k < front.size(); k++) {
                        pN.add(front.get(k));
                    }

                    //Decrement remain
                    remain = remain - front.size();

                    //Obtain the next front
                    index++;
                    if ((remain > 0) && (ranking.getNumberOfSubfronts() > index)) {
                        front = ranking.getSubfront(index);
                    }
                }

                if (remain > 0) {  // front contains individuals to insert                        
                    distance.crowdingDistanceAssignment(front, problem_.getNumberOfObjectives());
                    front.sort(new CrowdingComparator());
                    for (int k = 0; k < remain; k++) {
                        pN.add(front.get(k));
                    }
                    remain = 0;
                }
            }
            /**
             * **********************FIM NSGAII*******************************
             */

            /*Aproveita as solucoes geradas pelos operadores geneticos*/
            pObj1 = ((SolutionSet) pObj1).union(filhosSolucaoSet);
            pObj2 = ((SolutionSet) pObj2).union(filhosSolucaoSet);
            pPond = ((SolutionSet) pPond).union(filhosSolucaoSet);

            /*Ordena as populacoes para aproveitar as novas boas solucoes*/
            pObj1.sort(comparatorObj1_);
            pObj2.sort(comparatorObj2_);
            pPond.sort(comparatorPonderada_);

            retirarSolucoesExcedentes(pObj1, populacaoQtdPontas);
            retirarSolucoesExcedentes(pObj2, populacaoQtdPontas);
            retirarSolucoesExcedentes(pPond, populacaoQtdPonderada);

            // This piece of code shows how to use the indicator object into the code
            // of MOEANSM. In particular, it finds the number of evaluations required
            // by the algorithm to obtain a Pareto front with a hypervolume higher
            // than the hypervolume of the true Pareto front.
            if ((indicadores != null) && (requiredEvaluations == 0)) {
                double HVAtual = indicadores.getHypervolume(((SolutionSet) pN).union(pS));
                if (HV >= HVAtual && HV != 0.0) {
                    estagnacao++;
                } else {
                    HV = HVAtual;
                    estagnacao = 0;
                }
                if (estagnacao >= MAX_ESTAGNACAO) {
                    System.out.println("Abordagem Estagnou na geracao: " + geracao);
                    break;
                }
            }
        }

        // Retorna valores para arquidvo de saida
        setOutputParameter("requiredEvaluations", requiredEvaluations);

        populacaoUniao = ((SolutionSet) pN).union(pS).union(pTchebycheff);

        //Saida dos resultados
        Ranking ranking = new Ranking(populacaoUniao);
        ranking.getSubfront(0).printFeasibleFUNCSV("TESTE_ABORDAGEM");
        ranking.getSubfront(0).setTempoExecucao(tempoExecucao.getTempo());
        ranking.getSubfront(0).setGeracaoParou(geracao);

        // Retorna a primeira fronteira nao dominada
        return ranking.getSubfront(0);

    }

    public void aplicarBuscaLocalAleatorio(Solution[] offSpring, Operator buscaLocal2OPT) {
        int objetivoSelecionado = UtilAP.obterNumeroAleatorio(0, 3);
        if (objetivoSelecionado == 0) {
            offSpring[0].setObjetivoBuscaLocal(0);
            offSpring[1].setObjetivoBuscaLocal(0);
        } else if (objetivoSelecionado == 1) {
            offSpring[0].setObjetivoBuscaLocal(1);
            offSpring[1].setObjetivoBuscaLocal(1);
        } else if (objetivoSelecionado == 2) {
            offSpring[0].setObjetivoBuscaLocal(null);
            offSpring[1].setObjetivoBuscaLocal(null);
        }
        try {
            offSpring[0] = (Solution) buscaLocal2OPT.execute(offSpring[0]);
            offSpring[1] = (Solution) buscaLocal2OPT.execute(offSpring[1]);
        } catch (JMException ex) {
            ex.printStackTrace();
        }
    }

    private void retirarSolucoesExcedentes(SolutionSet pObj, int populacaoQtd) {
        /*Remove as solucoes excedentes*/
        for (int i = pObj.size() - 1; i >= populacaoQtd; i--) {
            pObj.remove(i);
        }
    }

    private void atualizarElitista(Solution individual) {
        for (int n = 0; n < problem_.getNumberOfObjectives(); n++) {
            if (individual.getObjective(n) < z_[n]) {
                z_[n] = individual.getObjective(n);

                indArray_ = individual;
            }
        }
    }

    private double funcaoFitness(Solution individual, double[] lambda) {
        double fitness = 0.0;
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
        }
        fitness = maxFun;
        return fitness;
    }
    /*
     Atualiza a populacao. Verifica se a solucao encontrada é melhor do que as que
     estao na populacao, caso seja, remove as piores
     */

    private void atualizarSolucoes(Solution solucao, SolutionSet populacao) {
        int timeC = 0;
        for (int i = 0; i < populacao.size(); i++) {
            double f1, f2;
            f1 = funcaoFitness(populacao.get(i), lambda_[i]);
            f2 = funcaoFitness(solucao, lambda_[i]);

            if (f2 < f1) {
                populacao.replace(i, new Solution(solucao));
                timeC++;
            }

            if (timeC >= nr_) {
                return;
            }
        }
    }

    public void inicializarMatrizPesos(int populacaoTchebyCheff) {
        if ((problem_.getNumberOfObjectives() == 2) && (populacaoTchebyCheff <= 300)) {
            for (int n = 0; n < populacaoTchebyCheff; n++) {
                double a = 1.0 * n / (populacaoTchebyCheff - 1);
                lambda_[n][0] = a;
                lambda_[n][1] = 1 - a;
            }
        }
    }
}
