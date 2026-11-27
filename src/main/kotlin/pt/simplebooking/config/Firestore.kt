
package pt.simplebooking.config

object Firestore {

    object Collections {
        const val AVAILABILITY_EXCEPTIONS = "availability_exceptions"
        const val BOOKABLE_RESOURCES = "bookable_resources"
        const val BOOKINGS = "bookings"
        const val BUSINESS_CLOSURES = "business_closures"
        const val BUSINESS_LOCKS = "business_locks"
        const val BUSINESS_SHOPS = "business_shops"
        const val PUSH_TOKENS = "push_tokens"
        const val RECURRING_BOOKING_SERIES = "recurring_booking_series"
        const val RESOURCE_LOCKS = "resource_locks"
        const val SERVICE_CATALOGS = "service_catalogs"
        const val TERMS_OF_SERVICE = "terms_of_service"
        const val TERMS_PRIVACY = "terms_privacy"
    }

    object Fields {

        object AvailabilityException {
            const val START_AT = "startAt"
            const val END_AT = "endAt"
            const val RESOURCE_ID = "resourceId"
            const val REASON = "reason"
            const val BUSINESS_ID = "businessId"
            const val DELETED_AT = "deletedAt"
            const val CREATED_AT = "createdAt"
            const val UPDATED_AT = "updatedAt"
        }

        object BookableResource {
            const val UID = "uid"
            const val NAME = "name"
            const val EMAIL_RESOURCE = "emailResource"
            const val ACTIVE = "active"
            const val BUSINESS_ID = "businessId"
            const val PHOTO_URL = "photoUrl"
            const val DELETED_AT = "deletedAt"
            const val CREATED_AT = "createdAt"
            const val UPDATED_AT = "updatedAt"
            const val SUNDAY = "sunday"
            const val MONDAY = "monday"
            const val TUESDAY = "tuesday"
            const val WEDNESDAY = "wednesday"
            const val THURSDAY = "thursday"
            const val FRIDAY = "friday"
            const val SATURDAY = "saturday"
        }

        object Booking {
            const val RESOURCE_ID = "resourceId"
            const val BUSINESS_ID = "businessId"
            const val CUSTOMER_NAME = "customerName"
            const val CUSTOMER_EMAIL = "customerEmail"
            const val CUSTOMER_PHONE = "customerPhone"
            const val ORIGIN = "origin"
            const val START_AT = "startAt"
            const val DURATION_MINUTES = "durationMinutes"
            const val SERVICE_ID = "serviceId"
            const val SERVICE_NAME = "serviceName"
            const val LIST_PRICE = "listPrice"
            const val DISCOUNT = "discount"
            const val DELETED_AT = "deletedAt"
            const val RESOURCE_EMAIL = "resourceEmail"
            const val CREATED_AT = "createdAt"
            const val UPDATED_AT = "updatedAt"
            const val SMS_REMINDER_SENT_AT = "smsReminderSentAt"
            const val RECURRING_SERIES_ID = "recurringSeriesId"
            const val RECURRENCE_INDEX = "recurrenceIndex"
        }

        object PushToken {
            const val UID = "uid"
            const val EMAIL = "email"
            const val TOKEN = "token"
            const val PLATFORM = "platform"
            const val CREATED_AT = "createdAt"
            const val LAST_SEEN_AT = "lastSeenAt"
        }

        object BusinessClosure {
            const val BUSINESS_ID = "businessId"
            const val NAME = "name"
            const val START_DATE = "startDate"
            const val END_DATE = "endDate"
            const val DELETED_AT = "deletedAt"
            const val CREATED_AT = "createdAt"
            const val UPDATED_AT = "updatedAt"
        }

        object BusinessShop {
            const val NAME = "name"
            const val SLUG = "slug"
            const val IMAGE_URL = "imageUrl"
            const val ZIP_CODE = "zipCode"
            const val ADDRESS = "address"
            const val LOCALITY = "locality"
            const val FLOOR = "floor"
            const val CLOSED_DAYS = "closedDays"
            const val DELETED_AT = "deletedAt"
            const val UPDATED_AT = "updatedAt"
        }

        object BusinessLock {
            const val UPDATED_AT = "updatedAt"
        }

        object ResourceLock {
            const val UPDATED_AT = "updatedAt"
        }

        object RecurringBookingSeries {
            const val ID = "id"
            const val BUSINESS_ID = "businessId"
            const val RESOURCE_ID = "resourceId"
            const val CUSTOMER_NAME = "customerName"
            const val SERVICE_ID = "serviceId"
            const val DAY_OF_WEEK = "dayOfWeek"
            const val START_TIME = "startTime"
            const val START_DATE = "startDate"
            const val RECURRENCE_TYPE = "recurrenceType"
            const val MONTHLY_WEEK = "monthlyWeek"
            const val STATUS = "status"
            const val OCCURRENCE_COUNT = "occurrenceCount"
            const val LAST_OCCURRENCE_START = "lastOccurrenceStart"
            const val NEEDS_ATTENTION = "needsAttention"
            const val DELETED_AT = "deletedAt"
            const val CREATED_AT = "createdAt"
            const val UPDATED_AT = "updatedAt"
            const val CANCELLED_AT = "cancelledAt"
            const val CANCELLED_FROM = "cancelledFrom"
        }

        object ServiceCatalog {
            const val BUSINESS_ID = "businessId"
            const val SERVICES = "services"
            const val CREATED_AT = "createdAt"
            const val UPDATED_AT = "updatedAt"
            const val DELETED_AT = "deletedAt"
        }
        object Service {
            const val ID = "id"
            const val NAME = "name"
            const val COLOR = "color"
            const val DURATION_MINUTES = "durationMinutes"
            const val PRICE = "price"
        }

        object TermsOfService {
            const val TITLE = "title"
            const val INDEX = "index"
            const val CONTENT = "content"
        }

        object TermsPrivacy {
            const val TITLE = "title"
            const val INDEX = "index"
            const val CONTENT = "content"
        }
    }
}
