package com.java_podio.code_gen.static_interface;

import java.util.Map;

import com.podio.item.Item;
import com.podio.item.ItemBadge;
import com.podio.rating.RatingType;
import com.podio.rating.RatingValuesMap;
import com.podio.rating.TypeRating;

public class PodioMapper {

    public static Item toItem(ItemBadge itemBadge) {
	Item result = new Item();

	result.setCurrentRevision(itemBadge.getCurrentRevision());
	result.setExternalId(itemBadge.getExternalId());
	result.setFields(itemBadge.getFields());
	result.setId(itemBadge.getId());
	result.setInitialRevision(itemBadge.getInitialRevision());
	// itemBadge.getLink() - not part of Item.
	result.setRatings(toRatingValuesMap(itemBadge.getRatings()));
	result.setTitle(itemBadge.getTitle());

	return result;
    }

    public static RatingValuesMap toRatingValuesMap(Map<RatingType, TypeRating> ratings) {
	if (ratings == null)
	    return null;
	RatingValuesMap result = new RatingValuesMap();
	for (RatingType rating : ratings.keySet()) {
	    result.set(rating.name(), result.get(rating));
	}
	return result;
    }

}
