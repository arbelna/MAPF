package Environment;

import BasicCBS.Solvers.I_Solver;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

public class ModularRunManager extends A_RunManager{

    @Override
    void setSolvers() {

    }

    @Override
    void setExperiments() {

    }

    public void setSolvers(Collection<? extends I_Solver> solvers){
        super.solvers = new ArrayList<>(solvers);
    }

    public void setSolvers(I_Solver... solvers){
        super.solvers = Arrays.asList(solvers);
    }

    public void setExperiments(Collection<? extends Experiment> experiments) {
        super.experiments = new ArrayList<>(experiments);
    }

    public void setExperiments(Experiment... experiments){
        super.experiments = Arrays.asList(experiments);
    }
}