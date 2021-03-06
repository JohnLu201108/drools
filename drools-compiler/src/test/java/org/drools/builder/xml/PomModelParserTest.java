package org.drools.builder.xml;

import org.drools.kproject.xml.MinimalPomParser;
import org.drools.kproject.xml.PomModel;
import org.junit.Test;
import org.kie.builder.ReleaseId;

import java.io.InputStream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class PomModelParserTest {
    
    @Test
    public void parsePom() {
        InputStream is = PomModelParserTest.class.getResourceAsStream( "pom.xml" );
        assertNotNull( is );
        
        PomModel pomModel = MinimalPomParser.parse( PomModelParserTest.class.getName().replace( '.', '/' ) + ".pom.xml", is );
        assertEquals( "groupId", pomModel.getReleaseId().getGroupId() );
        assertEquals( "artifactId", pomModel.getReleaseId().getArtifactId() );
        assertEquals( "version", pomModel.getReleaseId().getVersion() );
        
        assertEquals( "parentGroupId", pomModel.getParentReleaseId().getGroupId() );
        assertEquals( "parentArtifactId", pomModel.getParentReleaseId().getArtifactId() );
        assertEquals( "parentVersion", pomModel.getParentReleaseId().getVersion() );

        assertEquals( 1, pomModel.getDependencies().size() );
        ReleaseId dep = pomModel.getDependencies().get(0);
        assertEquals( "dep1GroupId", dep.getGroupId() );
        assertEquals( "dep1ArtifactId", dep.getArtifactId() );
        assertEquals( "dep1Version", dep.getVersion() );
    }
}
