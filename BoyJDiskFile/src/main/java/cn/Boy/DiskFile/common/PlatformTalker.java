package cn.Boy.DiskFile.common;

public class PlatformTalker {

    private static PlatformTalker instance = null;
    public static PlatformTalker getInstance(){
        if(instance==null){
            instance = new PlatformTalker();
        }
        return instance;
    }

    //readMe-mem，battery，crystalCluster，processing task analysis


    //teachKnowledgeAPI

    //learnKnowledgeAPI(take&pull)


    //platformOpsIncretion

    //modilityFriendSensorAPI--verify,add,remove,weightSetting,role&task allocation vote for cluster/friend node


    //modilityKnowSelfBadAPI--get bad report from cluster/friend node
}
