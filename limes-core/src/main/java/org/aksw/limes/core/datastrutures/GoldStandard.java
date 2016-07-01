package org.aksw.limes.core.datastrutures;

import org.aksw.limes.core.io.mapping.AMapping;
import org.aksw.limes.core.io.cache.Cache;
import org.aksw.limes.core.io.cache.MemoryCache;

import java.util.List;

/**
 * This class contains the gold standard mapping and the source and target URIs
 *
 * @author Mofeed Hassan (mounir@informatik.uni-leipzig.de)
 * @version 1.0
 * @since 1.0
 */
public class GoldStandard {
    /** the mapping of the gold standard*/
    public AMapping referenceMappings;
    /** a list of the source dataset URIs*/
    public List<String> sourceUris;
    /** a list of the target dataset URIs*/
    public List<String> targetUris;

    public GoldStandard(AMapping reference, List<String> sourceUris, List<String> targetUris) {
        super();
        this.referenceMappings = reference;
        this.sourceUris = sourceUris;
        this.targetUris = targetUris;
    }
    
    public GoldStandard(AMapping reference, Cache sourceUris, Cache targetUris) {
        super();
        this.referenceMappings = reference;
        this.sourceUris = sourceUris.getAllUris();
        this.targetUris = targetUris.getAllUris();
    }

    public GoldStandard(AMapping m) {
        this.referenceMappings = m;
    }

}
