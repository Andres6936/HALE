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
        
        var item = inv.getUnequippedItems().add("potionHealing", "Decent", 3);
        quickbar.addToFirstEmptySlot("potionHealing", "Decent");
		
        if (role.equals("Adept")) {
		    inv.addAndEquip("armor_leather_base", "Decent");
            inv.addAndEquip("crossbow", "Decent");
			inv.addAndEquip("boots_leather_base", "Decent");
			inv.addAndEquip("gloves_leather_base", "Decent");
			inv.addAndEquip("bolt");
			inv.getUnequippedItems().add("bolt", 200);
			inv.getUnequippedItems().add("quarterstaff", "Decent");
            
        } else if (role.equals("Mage")) {
		    inv.addAndEquip("armor_robe");
			inv.addAndEquip("bolt");
            inv.addAndEquip("quarterstaff", "Decent");
			inv.addAndEquip("boots_leather_base", "Decent");
			inv.getUnequippedItems().add("crossbow", "Decent");
			inv.getUnequippedItems().add("bolt", 200);
            
        } else if (role.equals("Priest")) {
		    inv.addAndEquip("armor_mail_base", "Decent");
            inv.addAndEquip("morningstar", "Decent");
            inv.addAndEquip("shield_heavy_base", "Decent");
			inv.addAndEquip("boots_mail_base", "Decent");
			inv.addAndEquip("gloves_mail_base", "Decent");
			inv.addAndEquip("helmet_mail_base", "Decent");
            
        } else if (role.equals("Rogue")) {
		    inv.addAndEquip("armor_leatherhard_base", "Decent");
			inv.addAndEquip("boots_leather_base", "Decent");
			inv.addAndEquip("gloves_leather_base", "Decent");
			inv.addAndEquip("helmet_leather_base", "Decent");
			inv.addAndEquip("arrow");
            inv.getUnequippedItems().add("shortbow", "Decent");
			inv.getUnequippedItems().add("arrow", 200);
            item = inv.getUnequippedItems().add("spikeTrap", "Decent", 2);
            quickbar.addToFirstEmptySlot("spikeTrap", "Decent");
            
        } else if (role.equals("Warrior")) {
		    inv.addAndEquip("armor_plate_base", "Decent");
            inv.addAndEquip("gloves_plate_base", "Decent");
            inv.addAndEquip("longsword", "Decent");
			inv.addAndEquip("shield_heavy_base", "Decent");
			inv.addAndEquip("boots_plate_base", "Decent");
			inv.addAndEquip("helmet_plate_base", "Decent");
			inv.addAndEquip("arrow");
			inv.getUnequippedItems().add("longbow", "Decent");
			inv.getUnequippedItems().add("arrow", 100);
        } else if (role.equals("Monk")) {
			inv.addAndEquip("armor_robe");
			inv.getUnequippedItems().add("potionHealing", "Good", 2);
		} else if (role.equals("SpellSword")) {
			inv.addAndEquip("armor_mail_base", "Decent");
            inv.addAndEquip("battleaxe", "Decent");
            inv.addAndEquip("shield_light_base", "Decent");
			inv.addAndEquip("boots_leather_base", "Decent");
			inv.addAndEquip("gloves_leather_base", "Decent");
			inv.addAndEquip("helmet_mail_base", "Decent");
		} else if (role.equals("Medium")) {
			inv.addAndEquip("armor_robe");
			inv.addAndEquip("bolt");
            inv.addAndEquip("shortspear", "Decent");
			inv.addAndEquip("boots_leather_base", "Decent");
			inv.getUnequippedItems().add("crossbow", "Decent");
			inv.getUnequippedItems().add("bolt", 200);
		}
	}
}
