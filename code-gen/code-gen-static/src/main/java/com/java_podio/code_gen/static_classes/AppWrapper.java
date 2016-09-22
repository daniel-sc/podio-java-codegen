package com.java_podio.code_gen.static_classes;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.ObjectMapper;

import com.podio.contact.Profile;
import com.podio.embed.Embed;
import com.podio.file.File;
import com.podio.item.FieldValuesUpdate;
import com.podio.item.FieldValuesView;
import com.podio.item.Item;
import com.podio.item.ItemCreate;
import com.podio.item.ItemUpdate;

public abstract class AppWrapper implements Serializable {

    static final long serialVersionUID = 1L;

    /**
     * Stores the original item, as retrieved by java-podio api.
     */
    protected Item originalItem;

    /**
     * This represents the internal Podio id of the item.
     */
    protected Integer podioId;

    /**
     * This represents the internal Podio revision of the item.
     */
    protected Integer podioRevision;

    /**
     * This represents the Podio title of the item.
     */
    protected String podioTitle;

    /**
     * This represents the Podio tags of the item.
     */
    protected List<String> podioTags;

    /**
     * Uploaded/associated files.
     */
    protected List<File> files;

    /**
     * Fills this objects values from {@code item}.<br>
     * Subclasses should extend this method!
     * 
     * @param item
     * @throws ParseException
     */
    public void setValue(Item item) throws ParseException {
	setOriginalItem(item);
	podioId = item.getId();
	if (item.getCurrentRevision() != null) {
	    podioRevision = item.getCurrentRevision().getRevision();
	}
	podioTitle = item.getTitle();
	podioTags = item.getTags();
	files = item.getFiles();
    }

    /**
     * Stores the original item, as retrieved by java-podio api. This could be a {@link ItemOrItemBadge}.
     */
    public Item getOriginalItem() {
	return originalItem;
    }

    /**
     * Stores the original item, as retrieved by java-podio api.
     */
    public void setOriginalItem(Item originalItem) {
	this.originalItem = originalItem;
    }

    /**
     * This represents the internal Podio id of the item.
     */
    public Integer getPodioId() {
	return podioId;
    }

    /**
     * This represents the internal Podio id of the item.
     */
    public void setPodioId(Integer podioId) {
	this.podioId = podioId;
    }

    /**
     * This represents the internal Podio revision of the item.
     */
    public Integer getPodioRevision() {
	return podioRevision;
    }

    /**
     * This represents the internal Podio revision of the item.
     */
    public void setPodioRevision(Integer podioRevision) {
	this.podioRevision = podioRevision;
    }

    /**
     * This represents the Podio title of the item.
     */
    public String getPodioTitle() {
	return podioTitle;
    }

    /**
     * This represents the Podio title of the item.
     */
    public void setPodioTitle(String podioTitle) {
	this.podioTitle = podioTitle;
    }

    /**
     * This represents the Podio tags of the item.
     */
    public List<String> getPodioTags() {
	return podioTags;
    }

    /**
     * This represents the Podio tags of the item.
     */
    public void setPodioTags(List<String> podioTags) {
	this.podioTags = podioTags;
    }

    /**
     * Uploaded/associated files.
     * 
     * @return
     * @see #setFiles(List)
     * @see #addFileById(Integer)
     * @see #getFileIds()
     */
    public List<File> getFiles() {
	return files;
    }

    /**
     * Uploaded/associated files. For adding a (new) file see
     * {@link #addFileById(Integer)}.
     * 
     * @param fileIds
     * @see #getFiles()
     * @see #addFileById(Integer)
     * @see #getFileIds()
     */
    public void setFiles(List<File> files) {
	this.files = files;
    }

    /**
     * Adds a (already uploaded) file by its id.
     * 
     * @param fileId
     * @see #setFiles(List)
     * @see #getFiles()
     * @see #getFileIds()
     */
    public void addFileById(Integer fileId) {
	File f = new File();
	f.setId(fileId);
	List<File> files = getFiles();
	if (files == null) {
	    files = new ArrayList<File>();
	}
	files.add(f);
	setFiles(files);
    }

    /**
     * @return all ids of associated files. Is never {@code null} - even if
     *         {@link #files} is {@code null}.
     * @see #setFiles(List)
     * @see #getFiles()
     * @see #addFileById(Integer)
     */
    public List<Integer> getFileIds() {
	List<Integer> result = new ArrayList<Integer>();
	if (getFiles() != null) {
	    for (File f : getFiles()) {
		result.add(f.getId());
	    }
	}
	return result;
    }

    /**
     * As {@link ItemCreate} inherits from {@link ItemUpdate} this method can be
     * used to generate updates!
     */
    public ItemCreate getItemCreate() {
	ItemCreate result = new ItemCreate();
	if (getAppExternalId() != null) {
	    result.setExternalId(getAppExternalId());
	}
	if (getPodioRevision() != null) {
	    result.setRevision(getPodioRevision());
	}
	if (getPodioTags() != null) {
	    result.setTags(getPodioTags());
	}
	List<FieldValuesUpdate> fieldValuesList = new ArrayList<FieldValuesUpdate>();
	result.setFields(fieldValuesList);

	if (getFiles() != null) { // getFiles()!=null indicates changed files..
	    result.setFileIds(getFileIds());
	}

	return result;
    }

    public abstract String getAppExternalId();

    public abstract Integer getAppId();

    public String toString() {
	String result = "AppWrapper [";
	result += ("originalItem=" + originalItem);
	result += (", podioId=" + podioId);
	result += (", podioRevision=" + podioRevision);
	result += (", podioTitle=" + podioTitle);
	result += (", podioTags=" + podioTags);
	result += (", files=" + files);
	return (result + "]");
    }

    /*
     * (non-Javadoc) Excludes #originalItem
     * 
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
	final int prime = 31;
	int result = 1;
	result = prime * result + ((files == null) ? 0 : files.hashCode());
	result = prime * result + ((podioId == null) ? 0 : podioId.hashCode());
	result = prime * result + ((podioRevision == null) ? 0 : podioRevision.hashCode());
	result = prime * result + ((podioTags == null) ? 0 : podioTags.hashCode());
	result = prime * result + ((podioTitle == null) ? 0 : podioTitle.hashCode());
	return result;
    }

    /*
     * (non-Javadoc) Excludes #originalItem
     * 
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
	if (this == obj)
	    return true;
	if (obj == null)
	    return false;
	if (getClass() != obj.getClass())
	    return false;
	AppWrapper other = (AppWrapper) obj;
	if (files == null) {
	    if (other.files != null)
		return false;
	} else if (!files.equals(other.files))
	    return false;
	if (podioId == null) {
	    if (other.podioId != null)
		return false;
	} else if (!podioId.equals(other.podioId))
	    return false;
	if (podioRevision == null) {
	    if (other.podioRevision != null)
		return false;
	} else if (!podioRevision.equals(other.podioRevision))
	    return false;
	if (podioTags == null) {
	    if (other.podioTags != null)
		return false;
	} else if (!podioTags.equals(other.podioTags))
	    return false;
	if (podioTitle == null) {
	    if (other.podioTitle != null)
		return false;
	} else if (!podioTitle.equals(other.podioTitle))
	    return false;
	return true;
    }

    /**
     * @return a list of referenced item ids
     */
    protected static List<Integer> parseAppField(FieldValuesView fieldValue) throws ParseException {
	List<Integer> result = new ArrayList<Integer>();
	List<Map<String, ?>> entries;
	entries = ((List<Map<String, ?>>) fieldValue.getValues());
	Iterator<Map<String, ?>> iterator = entries.iterator();
	while (iterator.hasNext()) {
	    @SuppressWarnings("unchecked")
	    Integer value = ((Integer) ((Map<String, ?>) iterator.next().get("value")).get("item_id"));
	    if (value != null) {
		result.add(value);
	    }
	}
	return result;
    }

    protected static FieldValuesUpdate getFieldValuesUpdateFromApp(List<Integer> ids, String externalId) {
	if (ids == null) {
	    return null;
	}
	ArrayList<Map<String, ?>> values = new ArrayList<Map<String, ?>>();
	for (Integer id : ids) {
	    values.add(java.util.Collections.singletonMap("value", id));
	}
	return new FieldValuesUpdate(externalId, values);
    }

    protected static <T extends PodioCategory> FieldValuesUpdate getFielddValuesUpdateFromMultiCategory(
	    List<T> selections, String externalId) {
	if (selections == null) {
	    return null;
	}
	ArrayList<Map<String, ?>> values = new ArrayList<Map<String, ?>>();
	for (T selection : selections) {
	    values.add(java.util.Collections.singletonMap("value", new Integer(selection.getPodioId())));
	}
	return new FieldValuesUpdate(externalId, values);
    }

    @SuppressWarnings("unchecked")
    protected static <T extends Enum<T>> List<T> parseMultiCategoryField(FieldValuesView f, Class<T> enumtype) {
	List<T> result = new ArrayList<T>();
	try {
	    for (Map<String, ?> value : f.getValues()) {
		Integer id = (Integer) ((java.util.Map<String, ?>) value.get("value")).get("id");
		Object enumvalue = enumtype.getMethod("byId", int.class).invoke(null, id.intValue());
		result.add((T) enumvalue);
	    }
	} catch (TypeNotPresentException e) {
	    e.printStackTrace();
	    return result;
	} catch (IllegalAccessException e) {
	    e.printStackTrace();
	    return result;
	} catch (IllegalArgumentException e) {
	    e.printStackTrace();
	    return result;
	} catch (InvocationTargetException e) {
	    e.printStackTrace();
	    return result;
	} catch (NoSuchMethodException e) {
	    e.printStackTrace();
	    return result;
	} catch (SecurityException e) {
	    e.printStackTrace();
	    return result;
	}
	return result;
    }

    protected static FieldValuesUpdate getFieldValuesUpdateFromContacts(List<Profile> profiles, String externalId) {
	if (profiles == null) {
	    return null;
	}
	ArrayList<Map<String, ?>> values = new ArrayList<Map<String, ?>>();
	for (Profile profile : profiles) {
	    values.add(java.util.Collections.singletonMap("value", new Integer(profile.getProfileId())));
	}
	return new FieldValuesUpdate(externalId, values);
    }

    protected static FieldValuesUpdate getFieldValuesUpdateFromEmbeds(List<Embed> embeds, String externalId) {
	if (embeds == null) {
	    return null;
	}
	ArrayList<Map<String, ?>> values = new ArrayList<Map<String, ?>>();
	for (Embed embed : embeds) {
	    values.add(java.util.Collections.singletonMap("embed", embed.getId()));
	}
	return new FieldValuesUpdate(externalId, values);
    }

    /**
     * Parses {@code f} to type {@code type} by using {@link JsonProperty}
     * annotations in {@code type}.
     * 
     * @param f
     * @param type
     * @param elementKey
     *            key of elements, such as "value" or "embed"
     * @return list of parsed fields - might be empty.
     */
    protected static <T> List<T> parseFieldJson(FieldValuesView f, Class<T> type, String elementKey) {
	ObjectMapper objectMapper = new ObjectMapper();
	objectMapper.configure(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);
	objectMapper.setDateFormat(new SimpleDateFormat("yyyy")); // -MM-dd
								  // HH:mm:ss

	List<T> result = new ArrayList<T>();
	List<Map<String, ?>> entries = ((List<Map<String, ?>>) f.getValues());
	Iterator<Map<String, ?>> iterator = entries.iterator();
	while (iterator.hasNext()) {

	    T element = objectMapper.convertValue(iterator.next().get(elementKey), type);
	    if (element != null) {
		result.add(element);
	    }
	}
	return result;
    }
}
