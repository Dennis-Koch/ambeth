package com.koch.ambeth.service;

import java.util.List;

import com.koch.ambeth.model.BlobObject;

public interface IBlobObjectService
{
	BlobObject getBlobObject(Integer id);

	List<BlobObject> getBlobObjects(Integer... id);

	List<BlobObject> getAllBlobObjects();

	void updateBlobObject(BlobObject blobObject);

	void deleteBlobObject(BlobObject blobObject);
}