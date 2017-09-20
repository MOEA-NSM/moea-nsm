package jmetal.metaheuristics.abordagem.experimentos;

import java.util.HashMap;
import jmetal.core.Algorithm;
import jmetal.experiments.Settings;
import jmetal.metaheuristics.spea2.SPEA2BuscaLocal;
import jmetal.operators.crossover.Crossover;
import jmetal.operators.crossover.CrossoverFactory;
import jmetal.operators.mutation.Mutation;
import jmetal.operators.mutation.MutationFactory;
import jmetal.operators.selection.Selection;
import jmetal.operators.selection.SelectionFactory;
import jmetal.problems.ProblemFactory;
import jmetal.util.JMException;

public class SPEA2BLConfiguracoes extends Settings {

    public int maxEvaluations_;

    public SPEA2BLConfiguracoes(String problem, int maxGeracoes) {
        super(problem);
        this.maxEvaluations_ = maxGeracoes;
        try {
            Object[] problemParams = {"Permutation"};
            problem_ = (new ProblemFactory()).getProblem(problemName_, problemParams);
        } catch (JMException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public Algorithm configure() throws JMException {
        Algorithm algorithm;
        Selection selection;
        Crossover crossover;
        Mutation mutation;
        Mutation buscaLocal = null;
        boolean aplicarBuscaLocal = true;

        algorithm = new SPEA2BuscaLocal(problem_);

        // Algorithm parameters
        algorithm.setInputParameter("populationSize", 100);
        algorithm.setInputParameter("archiveSize", 100);
        algorithm.setInputParameter("maxEvaluations", maxEvaluations_);

        HashMap parameters = new HashMap(); // Operator parameters
        parameters.put("probability", 0.80);
        crossover = CrossoverFactory.getCrossoverOperator("OXCrossover", parameters);
        /*Mutacao*/
        parameters = new HashMap();
        parameters.put("probability", 0.1);
        mutation = MutationFactory.getMutationOperator("SwapMutation", parameters);
        // Selection Operator
        parameters = null;
        selection = SelectionFactory.getSelectionOperator("BinaryTournament", parameters);

        if (aplicarBuscaLocal) {
            /*Busca Local*/
            parameters = new HashMap();
            parameters.put("probability", 0.1);
            parameters.put("problem", problem_);
            buscaLocal = MutationFactory.getMutationOperator("DoisOpt", parameters);
        }
        // Add the operators to the algorithm
        algorithm.addOperator("crossover", crossover);
        algorithm.addOperator("mutation", mutation);
        algorithm.addOperator("selection", selection);
        if (aplicarBuscaLocal) {
            algorithm.addOperator("buscaLocal", buscaLocal);
        }

        return algorithm;
    } // configure
} // NSGAII_Settings
