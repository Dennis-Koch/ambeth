package de.osthus.ambeth.service;

import java.util.List;

import de.osthus.ambeth.model.BlobObject;

public interface IBlobObjectService
{
	BlobObject getBlobObject(Integer id);

	List<BlobObject> getBlobObjects(Integer... id);

	List<BlobObject> getAllBlobObjects();

	void updateBlobObject(BlobObject blobObject);

	void deleteBlobObject(BlobObject blobObject);
}