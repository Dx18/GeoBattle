PACK = java -jar texturepacker.jar
ASSETS_DEST = ../android/assets
PACK_FILE = pack.json

all_: buildings_, building_icons_

buildings_:
	$(PACK) buildings $(ASSETS_DEST)/buildingsAtlas buildings $(PACK_FILE)

building_icons_:
	$(PACK) buildingIcons $(ASSETS_DEST)/buildingIconsAtlas buildingIcons $(PACK_FILE)

units_:
	$(PACK) units $(ASSETS_DEST)/unitsAtlas units $(PACK_FILE)

gui_:
	$(PACK) gui $(ASSETS_DEST)/skins/geoBattleSkin/ skin $(PACK_FILE)
