
function onAreaLoadFirstTime(game, area, transition) {
    game.showCutscene("intro");
    
    game.runExternalScript("quests/theMaster", "startQuest");
	
	// add default equipment to newly created characters
	for (var i = 0; i < game.getParty().size(); i++) {
        var partyMember = game.getParty().get(i);
	    
		if (!partyMember.inventory.isEmptyOtherThanDefaultClothes()) continue;
		
		// this is a newly created character, or at least a character with no items
		
		// strip the character of all equipment
		partyMember.inventory.clear();
		
		// add abilities to quickbar
		var quickbar = partyMember.quickbar;
        
        quickbar.clear();
        
        partyMember.abilities.fillEmptySlots();
		
		// add role appropriate items
		var role = partyMember.roles.getBaseRole().getID();
        var inv = partyMember.inventory;
        
        var item = inv.getUnequippedItems().add("potionHealing", "Fine", 3);
        quickbar.addToFirstEmptySlot("potionHealing", "Fine");
		
        if (role.equals("Adept")) {
		    inv.addAndEquip("armor_leather_base", "Fine");
            inv.addAndEquip("crossbow", "Fine");
			inv.addAndEquip("boots_leather_base", "Fine");
			inv.addAndEquip("gloves_leather_base", "Fine");
			inv.addAndEquip("bolt");
			inv.getUnequippedItems().add("bolt", 200);
			inv.getUnequippedItems().add("quarterstaff", "Fine");
            
        } else if (role.equals("Mage")) {
		    inv.addAndEquip("armor_robe");
			inv.addAndEquip("bolt");
            inv.addAndEquip("quarterstaff", "Fine");
			inv.addAndEquip("boots_leather_base", "Fine");
			inv.getUnequippedItems().add("crossbow", "Fine");
			inv.getUnequippedItems().add("bolt", 200);
            
        } else if (role.equals("Priest")) {
		    inv.addAndEquip("armor_plate_base", "Fine");
            inv.addAndEquip("morningstar", "Fine");
            inv.addAndEquip("shield_heavy_base", "Fine");
			inv.addAndEquip("boots_mail_base", "Fine");
			inv.addAndEquip("gloves_mail_base", "Fine");
			inv.addAndEquip("helmet_mail_base", "Fine");
            
        } else if (role.equals("Rogue")) {
		    inv.addAndEquip("armor_leatherhard_base", "Fine");
            inv.addAndEquip("shortSword", "Fine");
			inv.addAndEquip("boots_leather_base", "Fine");
			inv.addAndEquip("gloves_leather_base", "Fine");
			inv.addAndEquip("helmet_leather_base", "Fine");
			inv.addAndEquip("arrow");
            inv.getUnequippedItems().add("shortbow", "Fine");
			inv.getUnequippedItems().add("arrow", 200);
            item = inv.getUnequippedItems().add("spikeTrap", "Fine", 2);
            quickbar.addToFirstEmptySlot("spikeTrap", "Fine");
            
        } else if (role.equals("Warrior")) {
		    inv.addAndEquip("armor_plate_base", "Fine");
            inv.addAndEquip("gloves_plate_base", "Fine");
            inv.addAndEquip("longsword", "Fine");
			inv.addAndEquip("shield_heavy_base", "Fine");
			inv.addAndEquip("boots_plate_base", "Fine");
			inv.addAndEquip("helmet_plate_base", "Fine");
			inv.addAndEquip("arrow");
			inv.getUnequippedItems().add("longbow", "Fine");
			inv.getUnequippedItems().add("arrow", 100);
        } else if (role.equals("Monk")) {
			inv.addAndEquip("armor_robe");
			inv.getUnequippedItems().add("potionHealing", "Superb", 2);
		} else if (role.equals("SpellSword")) {
			inv.addAndEquip("armor_mail_base", "Fine");
            inv.addAndEquip("battleaxe", "Fine");
            inv.addAndEquip("shield_light_base", "Fine");
			inv.addAndEquip("boots_mail_base", "Fine");
			inv.addAndEquip("gloves_mail_base", "Fine");
			inv.addAndEquip("helmet_mail_base", "Fine");
		} else if (role.equals("Medium")) {
			inv.addAndEquip("armor_robe");
			inv.addAndEquip("bolt");
            inv.addAndEquip("shortspear", "Fine");
			inv.addAndEquip("boots_leather_base", "Fine");
			inv.getUnequippedItems().add("crossbow", "Fine");
			inv.getUnequippedItems().add("bolt", 200);
		}
	}
}
