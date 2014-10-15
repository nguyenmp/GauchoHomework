package com.nguyenmp.gauchodroid.common;

import android.view.Menu;
import android.view.MenuItem;

/**
 * Menu utilities that are meant to make the process of using menus easier
 * @author Mark
 *
 */
public class MenuUtils {
	/**
	 * Tries to add a new menu item without duplicates.
	 * @param title the title of the item
	 * @return true if the item was successfully added.  false if 
	 * it already existed.
	 */
	public static MenuItem addMenuItem(Menu menu, CharSequence title) {
		MenuItem menuItem = null;
		
		//Loop through the menu and try to find 
		//a menu item with the given title
		for (int i = 0; i < menu.size(); i++) {
			if (menu.getItem(i).getTitle().equals(title)) {
				menuItem = menu.getItem(i);
				break;
			}
		}
		
		//If we didn't find an existing menu item, 
		//create a new one with the given title
		if (menuItem == null) {
			menuItem = menu.add(title);
		}
		
		//Return either the new MenuItem or the 
		//existing MenuItem with the given title
		return menuItem;
	}
}
