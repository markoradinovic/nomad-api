package io.github.zanella.nomad;

import io.github.zanella.nomad.v1.nodes.models.NodeAllocation;
import io.github.zanella.nomad.v1.nodes.models.NodeInfo;
import io.github.zanella.nomad.v1.nodes.models.NodeSummary;

import java.util.List;

public class App {
    public static void main( String[] args ) throws Exception {
        final NomadClient nomadClient = new NomadClient("virtual.box.c7");

        ///
        for (NodeSummary nodeSummary : nomadClient.v1.nodes.getNodes()) {
            System.out.println("\n\t(DBG) nodeSummary: " + nodeSummary);
        }

        final String nodeId = nomadClient.v1.nodes.getNodesOfRegion("global").get(0).getId();

        ///
        final NodeInfo nodeInfo = nomadClient.v1.node.getNode(nodeId);
        final NodeInfo nodeInfoWithRegion = nomadClient.v1.node.getNodeForRegion(nodeId, "global");

        assert (nodeInfo.equals(nodeInfoWithRegion));

        System.out.println("\n\t(DBG) nodeInfo: " + nodeInfo);

        final List<NodeAllocation> nodeAllocationList = nomadClient.v1.node.getNodeAllocations(nodeId);
        for (NodeAllocation nodeAllocation : nodeAllocationList) {
            System.out.println("\n\t(DBG) nodeAllocation: " + nodeAllocation);

            System.out.println("\n\t *** nodeAllocation.getNodeJob(): " + nodeAllocation.getNodeJob());
        }
    }
}