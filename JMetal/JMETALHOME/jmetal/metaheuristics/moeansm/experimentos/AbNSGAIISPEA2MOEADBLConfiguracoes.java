package jmetal.metaheuristics.moeansm.experimentos;

import java.util.HashMap;
import jmetal.core.Algorithm;
import jmetal.experiments.Settings;
import jmetal.metaheuristics.moeansm.util.UtilAP;
import jmetal.metaheuristics.moeansm.moeansm.MoeaNSM;
import jmetal.operators.crossover.Crossover;
import jmetal.operators.crossover.CrossoverFactory;
import jmetal.operators.mutation.Mutation;
import jmetal.operators.mutation.MutationFactory;
import jmetal.operators.selection.Selection;
import jmetal.operators.selection.SelectionFactory;
import jmetal.problems.ProblemFactory;
import jmetal.util.JMException;

/**
 *
 * @author Deyvid
 */
class AbNSGAIISPEA2MOEADBLConfiguracoes extends Settings {

    public int maxGeracoes;

    public AbNSGAIISPEA2MOEADBLConfiguracoes(String problem, int maxGeracoes) {
        super(problem);
        this.maxGeracoes = maxGeracoes;
        try {
            Object[] problemParams = {"Permutation"};
            problem_ = (new ProblemFactory()).getProblem(problemName_, problemParams);
        } catch (JMException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Algorithm configure() throws JMException {
        Algorithm algorithm;
        Selection selection;
        Crossover crossover;
        Mutation mutation;
        Mutation buscaLocal;

        algorithm = new MoeaNSM(problem_);

        // Algorithm parameters
        int populacaoTotal = 100;
        algorithm.setInputParameter("populacaoQtdPontas", UtilAP.calcularPorcentagem(30, populacaoTotal));
        algorithm.setInputParameter("populacaoQtdPonderada", UtilAP.calcularPorcentagem(40, populacaoTotal));
        algorithm.setInputParameter("populacaoQtdNSGAII", UtilAP.calcularPorcentagem(60, populacaoTotal));
        algorithm.setInputParameter("populacaoQtdSPEA2", UtilAP.calcularPorcentagem(60, populacaoTotal));
        algorithm.setInputParameter("populacaoTchebyCheff", UtilAP.calcularPorcentagem(100, populacaoTotal));
        algorithm.setInputParameter("maxGeracoes", maxGeracoes);

        HashMap parameters = new HashMap(); // Operator parameters
        parameters.put("probability", 0.80);
        crossover = CrossoverFactory.getCrossoverOperator("OXCrossover", parameters);
        /*Busca Local*/
        parameters = new HashMap();
        parameters.put("probability", 0.1);
        mutation = MutationFactory.getMutationOperator("SwapMutation", parameters);
        /*Mutacao*/
        parameters = new HashMap();
        parameters.put("probability", 0.1);
        parameters.put("problem", problem_);
        buscaLocal = MutationFactory.getMutationOperator("DoisOpt", parameters);

        // Selection Operator
        parameters = null;
        selection = SelectionFactory.getSelectionOperator("BinaryTournament", parameters);
        // Add the operators to the algorithm
        algorithm.addOperator("crossover", crossover);
        algorithm.addOperator("mutacao", mutation);
        algorithm.addOperator("selecao", selection);
        algorithm.addOperator("buscaLocal2OPT", buscaLocal);

        return algorithm;
    }
}
