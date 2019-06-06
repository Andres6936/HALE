function onAreaLoadFirstTime(game, area) {

    game.showCutscene("intro");
    
    var value = game.getPartyCurrency().getValue();
    if (value < 50000) {
		// set value to 5 GP
        game.getPartyCurrency().setValue(50000);
    }
    
    for (var i = 0; i < game.getParty().size(); i++) {
        var partyMember = game.getParty().get(i);
		
	// strip the characters of all equipment
	partyMember.inventory.clear();
		
        var quickbar = partyMember.quickbar;
        
        quickbar.clear();
        
        partyMember.abilities.fillEmptySlots();
        
        var role = partyMember.roles.getBaseRole().getID();
        var inv = partyMember.inventory;
        
        var item = inv.getUnequippedItems().add("potionHealing", "Mediocre");
        quickbar.addToFirstEmptySlot("potionHealing", "Mediocre");
        
		inv.addAndEquip("boots_leather_base", "Mediocre");
		
        if (role.equals("Adept")) {
		    inv.addAndEquip("armor_robe");
            inv.addAndEquip("dagger", "Mediocre");
            
            item = inv.getUnequippedItems().add("potionCha", "Mediocre");
            quickbar.addToFirstEmptySlot("potionCha", "Mediocre");
            
        } else if (role.equals("Mage")) {
		    inv.addAndEquip("armor_robe");
            inv.addAndEquip("quarterstaff", "Mediocre");
            
            item = inv.getUnequippedItems().add("potionInt", "Mediocre");
            quickbar.addToFirstEmptySlot("potionInt", "Mediocre");
            
        } else if (role.equals("Priest")) {
		    inv.addAndEquip("armor_clothes");
            inv.addAndEquip("mace", "Mediocre");
            inv.addAndEquip("shield_light_base", "Mediocre");
            
            item = inv.getUnequippedItems().add("potionWis", "Mediocre");
            quickbar.addToFirstEmptySlot("potionWis", "Mediocre");
            
        } else if (role.equals("Rogue")) {
		    inv.addAndEquip("armor_clothes");
            inv.addAndEquip("dagger", "Mediocre");
            
            item = inv.getUnequippedItems().add("spikeTrap", "Mediocre", 2);
            quickbar.addToFirstEmptySlot("spikeTrap", "Mediocre");
            
            item = inv.getUnequippedItems().add("potionDex", "Mediocre");
            quickbar.addToFirstEmptySlot("potionDex", "Mediocre");
            
        } else if (role.equals("Warrior")) {
		    inv.addAndEquip("armor_clothes");
            inv.addAndEquip("gloves_leather_base", "Mediocre");
            inv.addAndEquip("shortSword", "Mediocre");
            
            item = inv.getUnequippedItems().add("potionStrength", "Mediocre");
            quickbar.addToFirstEmptySlot("potionStrength", "Mediocre");
            
        }
        
    }
}
