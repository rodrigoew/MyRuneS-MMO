package io.xeros.content.dialogue.types;


import io.xeros.content.dialogue.DialogueAction;
import io.xeros.content.dialogue.DialogueBuilder;
import io.xeros.content.dialogue.DialogueExpression;
import io.xeros.content.dialogue.DialogueObject;
import io.xeros.model.definitions.NpcDef;
import io.xeros.model.entity.player.Player;

public class NpcDialogue extends DialogueObject {

    /**
     * This array contains the child id where the dialogue
     * statement starts for npc and item dialogues.
     */
    private static final int[] NPC_DIALOGUE_ID = {
            4885,
            4890,
            4896,
            4903
    };

    private final int npcId;
    private final java.util.List<String[]> pages=new java.util.ArrayList<>();
    private int page;
    private boolean finished;
    private final DialogueExpression expression;

    public NpcDialogue(DialogueBuilder context, int npcId, DialogueExpression expression, String[] text) {
        super(context, true);
        this.npcId = npcId;
        java.util.List<String> lines=io.xeros.content.locale.PtBr.dialogueLines(text,54);
        for(int i=0;i<lines.size();i+=4)pages.add(lines.subList(i,Math.min(i+4,lines.size())).toArray(new String[0]));
        this.expression = expression;
    }

    @Override
    public void send(Player player) {
        if(finished){page=0;finished=false;}
        String[] text=pages.get(page);
        int startDialogueChildId = NPC_DIALOGUE_ID[text.length - 1];
        int headChildId = startDialogueChildId - 2;
        player.getPA().sendNpcHeadOnInterface(npcId, headChildId);
        player.getPA().sendInterfaceAnimation(headChildId, expression.getAnimation());
        player.getPA().sendName(startDialogueChildId - 1, NpcDef.forId(npcId) != null ? NpcDef.forId(npcId).getName().replaceAll("_", " ") : "");
        for (int i = 0; i < text.length; i++) {
            player.getPA().sendString(startDialogueChildId + i, text[i]);
        }
        player.getPA().sendChatboxInterface(startDialogueChildId - 3);
    }

    @Override
    public void handleAction(Player player, DialogueAction action) {
        if (action == DialogueAction.CLICK_TO_CONTINUE) {
            if(page+1<pages.size()){page++;send(player);}else{finished=true;getContext().sendNextDialogue();}
        }
    }

}
