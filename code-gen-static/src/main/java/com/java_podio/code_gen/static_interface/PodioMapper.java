package com.java_podio.code_gen.static_interface;

import java.util.Map;

import com.java_podio.code_gen.static_classes.ItemOrItemBadge;
import com.podio.item.ItemBadge;
import com.podio.rating.RatingType;
import com.podio.rating.RatingValuesMap;
import com.podio.rating.TypeRating;

public class PodioMapper {

    public static ItemOrItemBadge toItem(ItemBadge itemBadge) {
    	ItemOrItemBadge result = new ItemOrItemBadge();

	result.setCurrentRevision(itemBadge.getCurrentRevision());
	result.setExternalId(itemBadge.getExternalId());
	result.setFields(itemBadge.getFields());
	result.setId(itemBadge.getId());
	result.setInitialRevision(itemBadge.getInitialRevision());
	result.setLink(itemBadge.getLink());
	result.setFileCount(itemBadge.getFiles());
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
