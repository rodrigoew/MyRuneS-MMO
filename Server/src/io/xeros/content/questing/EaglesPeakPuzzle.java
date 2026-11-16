package io.xeros.content.questing;

/** Saved mechanisms are independent of the journal stage and of temporary scene objects. */
final class EaglesPeakPuzzle {
    int dialogue, silver, bronzeWinches, levers, birds;
    boolean bronzeRaised, bronzeTriggered, bronzeInserted, goldInserted, lessonGiven, goldWestVisited;
    private static final int A=1, B=2, C=4, D=8;
    boolean allFeathers(){return bronzeInserted&&goldInserted&&silver==6;}
    boolean bronzeSolved(){return bronzeTriggered&&bronzeWinches==15&&!bronzeRaised;}
    void triggerBronze(){if(!bronzeTriggered){bronzeTriggered=true;bronzeRaised=true;}}
    boolean winch(int index){if(!bronzeRaised||index<0||index>3)return false;bronzeWinches|=1<<index;if(bronzeWinches==15)bronzeRaised=false;return true;}
    boolean lever(int object){int bit=object==19946?A:object==19947?B:object==19948?C:object==19949?D:0;if(bit==0)return false;levers^=bit;return true;}
    boolean down(int bit){return (levers&bit)!=0;}
    boolean moved(int bird){return (birds&(1<<bird))!=0;}
    private void move(int bird,boolean destination){if(destination)birds|=1<<bird;else birds&=~(1<<bird);}
    /** Feeders attract birds along their own rails; closed wings can isolate the shared L-shaped track. */
    boolean feed(int object){
        switch(object){
            case 19939:move(0,true);return true;
            case 19944:move(0,false);return true;
            case 19938:move(1,true);return true;
            case 19943:move(1,false);return true;
            case 19936:move(3,true);return true;
            case 19940:move(3,false);return true;
            case 19937:
                // The central/south wings separate the two birds sharing this corner.
                if(!down(C)&&down(D)&&!down(A)){move(2,true);return true;}
                if(down(A)&&down(B)){move(2,true);if(goldWestVisited)move(4,true);return true;}
                return false;
            case 19942:move(2,false);return true;
            case 19941:
                if(!down(C)&&down(D)&&down(A)&&down(B)&&moved(2)){goldWestVisited=true;move(2,false);move(4,false);return true;}
                return false;
            default:return false;
        }
    }
    void resetGold(){levers=birds=0;goldWestVisited=false;}
    boolean goldReachable(){return !down(C)&&down(D)&&down(A)&&down(B)&&moved(4);}
    int config934(int stage){
        int value=stage*5|(bronzeRaised?1<<6:0)|levers<<14|silver<<23|bronzeWinches<<26;
        if(moved(0))value|=1<<22;if(moved(1))value|=1<<21;if(moved(2))value|=1<<19;
        if(moved(3))value|=1<<18;if(moved(4))value|=1<<20;
        if(goldInserted)value|=1<<30;if(bronzeInserted)value|=1<<31;return value;
    }
    String encode(){return "2,"+dialogue+","+silver+","+bronzeWinches+","+levers+","+birds+","+(bronzeRaised?1:0)+","+(bronzeTriggered?1:0)+","+(bronzeInserted?1:0)+","+(goldInserted?1:0)+","+(lessonGiven?1:0)+","+(goldWestVisited?1:0);}
    void restore(String record){
        if(record==null||!record.matches("2,[0-5],[0-6],(?:[0-9]|1[0-5]),(?:[0-9]|1[0-5]),(?:[0-9]|[12][0-9]|3[01]),[01],[01],[01],[01],[01],[01]"))return;
        String[] fields=record.split(",");int[] n=new int[fields.length];for(int i=0;i<n.length;i++)n[i]=Integer.parseInt(fields[i]);
        // Reject impossible partial records rather than making winches or rewards repeatable.
        if(n[6]==1&&n[7]==0||n[3]!=0&&n[7]==0||n[6]==1&&n[3]==15)return;
        dialogue=n[1];silver=n[2];bronzeWinches=n[3];levers=n[4];birds=n[5];bronzeRaised=n[6]==1;bronzeTriggered=n[7]==1;bronzeInserted=n[8]==1;goldInserted=n[9]==1;lessonGiven=n[10]==1;goldWestVisited=n[11]==1;
    }
}
