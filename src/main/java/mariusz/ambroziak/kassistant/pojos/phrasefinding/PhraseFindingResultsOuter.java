package mariusz.ambroziak.kassistant.pojos.phrasefinding;

import java.util.List;

public class PhraseFindingResultsOuter {
    PhraseFindingResults inner;

    public PhraseFindingResults getInner() {
        return inner;
    }

    public void setInner(PhraseFindingResults inner) {
        this.inner = inner;
    }


    public PhraseFindingResultsOuter(PhraseFindingResults inner) {
        this.inner = inner;
    }

    public PhraseFindingResultsOuter() {
    }
}
