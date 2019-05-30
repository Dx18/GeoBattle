package geobattle.geobattle.tutorial;

// Game tutorial
public final class Tutorial {
    // Steps of tutorial
    private final TutorialStep[] steps;

    // Index of next step of tutorial
    private int currentStep;

    public Tutorial(TutorialStep[] steps) {
        this.steps = steps;
        currentStep = 0;
    }

    // Returns current step of tutorial
    public TutorialStep getCurrent() {
        if (currentStep >= steps.length)
            return null;
        return steps[currentStep];
    }

    public void nextStep() {
        currentStep++;
    }
}
