package com.client.graphics.interfaces.settings;

import com.client.TextDrawingArea;
import com.client.graphics.interfaces.RSInterface;
import com.client.graphics.interfaces.dropdown.*;
import com.client.graphics.interfaces.impl.Interfaces;

import java.util.Arrays;

public class SettingsInterface extends RSInterface {

    public static final Setting OLD_GAMEFRAME = new Setting("Estilo da interface", 1, new OldGameframeMenu(), "2006", "OSRS");
    public static final Setting GAME_TIMERS = new Setting("Temporizadores", 0, new GameTimersMenu(), "Ligado", "Desligado");
    public static final Setting ANTI_ALIASING = new Setting("Suavizar contornos", 1, new AntiAliasingMenu(), "Ligado", "Desligado");
    public static final Setting GROUND_ITEM_NAMES = new Setting("Nomes no chão", 1, new GroundItemOverlayMenu(), "Ligado", "Desligado");
    public static final Setting FOG = new Setting("Neblina", 0, new FogMenu(), "Ligado", "Desligado");
    public static final Setting SMOOTH_SHADING = new Setting("Sombras suaves", 0, new SmoothShadingMenu(), "Ligado", "Desligado");
    public static final Setting TILE_BLENDING = new Setting("Misturar terrenos", 1, new TileBlendingMenu(), "Ligado", "Desligado");
    public static final Setting STATUS_BARS = new Setting("Barras de oração e vida", 1, new StatusBarsMenu(), "Ligado", "Desligado");
    public static final Setting DRAG = new Setting("Arrastar", 0, new DragMenu(), "5 (padrão)", "6", "8", "10", "12");
    public static final Setting PVP_TAB = new Setting("Aba PvP", 0, new PvpTab(), "Ligado", "Desligado");
    public static final Setting ROOF = new Setting("Mostrar telhados", 0, new EnableRoofs(), "Ligado", "Desligado");
    public static final Setting MENU_HOVERS = new Setting("Realce dos menus", 0, new MenuHovers(), "Desligado", "Ligado");
    public static final Setting STRETCHED_MODE = new Setting("Interface ampliada", 1, new StretchedModeMenu(), "Ligado", "Desligado");
    public static final Setting PLAYER_PROFILE = new Setting("Aba do perfil", 0, new PlayerProfile(), "Ligado", "Desligado");

    public static final Setting INVENTORY_MENU = new Setting("Menu da mochila", 1, new InventoryHoverMenu(), "Desligado", "On (Magenta)", "On (Lime green)", "On (Cyan)", "On (Red)");
    public static final Setting BOUNTY_HUNTER = new Setting("Bounty Hunter", 0, new BountyHunterMenu(), "Ligado", "Desligado");
    public static final Setting ENTITY_TARGET = new Setting("Alvo selecionado", 0, new EntityTargetOverlayMenu(), "Ligado", "Desligado");
    public static final Setting CHAT_EFFECT = new Setting("Efeito do chat", 0, new ChatEffectMenu(), "Amarelo (padrão)", "Vermelho","Verde","Ciano","Roxo","Branco","Flash 1","Flash 2", "Flash 3","Glow 1", "Glow 2","Glow 3");
    public static final Setting DRAW_DISTANCE = new Setting("Distância de visão", 0, new DrawDistanceMenu(), "30 (padrão)", "40", "50", "60", "70");
    public static final Setting PM_NOTIFICATION = new Setting("Aviso de mensagem privada", 1, new PmNotificationMenu(), "Ligado", "Desligado");

    private int childInterfaceId;
    private int childIndex;
    private int yOffset = 0;
    private RSInterface scroll;

    public SettingsInterface() {
    }

    public void load(TextDrawingArea[] tda) {
        RSInterface tab = addInterface(39000);
        addSprite(39001, 0, "/Interfaces/settings/IMAGE");
        addText(39002, "Configurações avançadas", tda, 2, 0xFFA500, true, true);
        String dir = "/Interfaces/Prestige/SPRITE";
        addHoverButton(39021, dir, 1, 16, 16, "Fechar", -1, 39022, 3);
        addHoveredButton(39022, dir, 2, 16, 16, 39023);

        setChildren(5,tab);
        int index = 0;
        setBounds(39001,16,16,index++,tab);
        setBounds(39002,253,24,index++,tab);
        setBounds(39003,30,60,index++,tab);
        setBounds(39021,478,24,index++,tab);
        setBounds(39022,478,24,index++,tab);

        scroll = addInterface(39003);
        scroll.width = 448;
        scroll.height = 246;
        childInterfaceId = 39023;
        childIndex = 0;

        SettingsWidgetSection[] sections = {
                new SettingsWidgetSection("Opções da interface", OLD_GAMEFRAME, INVENTORY_MENU, BOUNTY_HUNTER, ENTITY_TARGET, GAME_TIMERS, CHAT_EFFECT, GROUND_ITEM_NAMES, PM_NOTIFICATION),
                new SettingsWidgetSection("Opções gráficas", STRETCHED_MODE, DRAW_DISTANCE, ANTI_ALIASING, FOG, SMOOTH_SHADING, TILE_BLENDING , STATUS_BARS , DRAG , PVP_TAB, ROOF, MENU_HOVERS, PLAYER_PROFILE),
        };

        Arrays.stream(sections).forEach(section -> {
            expandChildren(1, scroll);
            header(section);
            Arrays.stream(section.settings).forEach(setting -> {
                expandChildren(2, scroll);
                build(setting);
            });
        });

        scroll.reverseChildren();
        scroll.scrollMax = yOffset + 80;//32
    }

    public void header(SettingsWidgetSection section) {
        addText(childInterfaceId, section.sectionName, Interfaces.defaultTextDrawingAreas, 2, 0xFFA500, false, true);
        setBounds(childInterfaceId,24, yOffset, childIndex, scroll);
        yOffset += 20;
        childInterfaceId++;
        childIndex++;
    }

    public void build(Setting setting) {
        setting.setInterfaceId(childInterfaceId + 1);
        addText(childInterfaceId, setting.getSettingName(), Interfaces.defaultTextDrawingAreas, 1, 0xFFA500, false, true);
        dropdownMenu(setting.getInterfaceId(), 166,setting.getDefaultOption(), setting.getOptions(), setting.getMenuItem(), Interfaces.defaultTextDrawingAreas, 1);
        setBounds(childInterfaceId,32, yOffset, childIndex, scroll);
        setBounds(childInterfaceId + 1,260, yOffset, childIndex + 1, scroll);
        childInterfaceId += 2;
        childIndex += 2;
        yOffset += 25;
    }

    private static class SettingsWidgetSection {

        private final String sectionName;
        private final Setting[] settings;

        public SettingsWidgetSection(String sectionName, Setting...settings) {
            this.sectionName = sectionName;
            this.settings = settings;
        }
    }
}
