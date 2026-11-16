package io.xeros.content.dialogue.types;

import io.xeros.content.dialogue.DialogueAction;
import io.xeros.content.dialogue.DialogueBuilder;
import io.xeros.content.dialogue.DialogueExpression;
import io.xeros.content.dialogue.DialogueObject;
import io.xeros.model.entity.player.Player;
import io.xeros.util.Misc;

public class PlayerDialogue extends DialogueObject {

    /**
     * This array contains the child id where the dialogue
     * statement starts for player dialogues.
     */
    private static final int[] PLAYER_DIALOGUE_ID = {
            971,
            976,
            982,
            989
    };

    private final java.util.List<String[]> pages=new java.util.ArrayList<>();
    private int page;
    private boolean finished;
    private final DialogueExpression expression;

    public PlayerDialogue(DialogueBuilder context, DialogueExpression expression, String[] text) {
        super(context, true);
        java.util.List<String> lines=io.xeros.content.locale.PtBr.dialogueLines(text,54);
        for(int i=0;i<lines.size();i+=4)pages.add(lines.subList(i,Math.min(i+4,lines.size())).toArray(new String[0]));
        this.expression = expression;
    }

    @Override
    public void send(Player player) {
        if(finished){page=0;finished=false;}
        String[] text=pages.get(page);
        int startDialogueChildId = PLAYER_DIALOGUE_ID[text.length - 1];
        int headChildId = startDialogueChildId - 2;
        player.getPA().sendPlayerHeadOnInterface(headChildId);
        player.getPA().sendInterfaceAnimation(headChildId, expression.getAnimation());
        player.getPA().sendName(startDialogueChildId - 1, player.getDisplayName());
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
