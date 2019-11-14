package com.java_podio.code_gen.static_classes;

import com.podio.item.Item;

public class ItemOrItemBadge extends Item {

	private static final long serialVersionUID = 1L;

	private String link;

	private int fileCount;
	
	public ItemOrItemBadge() {
	}

	public ItemOrItemBadge(Item item) {
		setId(item.getId());
		setApplication(item.getApplication());
		setComments(item.getComments());
		setCurrentRevision(item.getCurrentRevision());
		setExternalId(item.getExternalId());
		setFields(item.getFields());
		setFiles(item.getFiles());
		setInitialRevision(item.getInitialRevision());
		setRatings(item.getRatings());
		setRevisions(item.getRevisions());
		setSubscribed(item.isSubscribed());
		setTags(item.getTags());
		setTitle(item.getTitle());
		setUserRatings(item.getUserRatings());
	}

	public String getLink() {
		return link;
	}

	public void setLink(String link) {
		this.link = link;
	}

	public int getFileCount() {
		return fileCount == 0 && getFiles() != null ? getFiles().size() : fileCount;
	}

	public void setFileCount(int fileCount) {
		this.fileCount = fileCount;
	}

}
