package pt.simplebooking.repository

import com.google.cloud.firestore.Firestore
import org.springframework.stereotype.Repository
import pt.simplebooking.config.Firestore as FirestoreConstants
import pt.simplebooking.mapper.BookableResourceMapper
import pt.simplebooking.models.bo.BookableResource

@Repository
class BookableResourceRepository(
    private val firestore: Firestore
) {

    fun findByUid(uid: String): BookableResource? {
        val document = firestore
            .collection(FirestoreConstants.Collections.BOOKABLE_RESOURCES)
            .whereEqualTo(FirestoreConstants.Fields.BookableResource.UID, uid)
            .limit(1)
            .get()
            .get()
            .documents
            .firstOrNull()
            ?: return null

        return BookableResourceMapper.fromMap(
            id = document.id,
            map = document.data
        )
    }
}
