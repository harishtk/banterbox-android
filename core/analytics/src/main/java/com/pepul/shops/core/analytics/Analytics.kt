package com.pepul.shops.core.analytics

import androidx.annotation.Size

@Suppress("SpellCheckingInspection")
object Analytics {

    /**
     * An `object` to hold the custom Firebase Analytics Events
     */
    object Event {
        /* Onboard */
        const val ONBOARD_MOBILE_NUMBER_SCREEN_PRESENTED = "Onboarding_Mobile_Screen_presented"
        const val ONBOARD_GET_OTP_BUTTON_EVENT = "Onboarding_Get_OTPbutton"
        const val ONBOARD_AUTOFILL_PHONE_NUMBER = "Onboarding_autoFillnumber"
        const val ONBOARD_OTP_SUCCESS_EVENT = "Onboarding_OTPsuccess"
        const val ONBOARD_OTP_FAIL_EVENT = "Onboarding_OTPfail"
        const val ONBOARD_CUSTOMIZED_FIRST_OPEN = "Customized_firstopen"

        const val ONBOARD_LEGAL_ACCEPT_CLICK = "Onboard_legal_acceptClick"
        const val ONBOARD_LEGAL_READ_TERMS = "Onboard_legal_read_terms"
        const val ONBOARD_LEGAL_READ_PRIVACY = "Onboard_legal_read_privacy"
        const val ONBOARD_ADD_INFO_PRESENTED = "Onboard_addInfo_page_presented"
        /* END - Onboard */

        /* Login */
        const val LOGIN_OTP_AUTOFILL = "Login_otp_autoFill"
        const val LOGIN_RESEND_OTP_CLICK = "Login_resendOtp_click"
        const val LOGIN_OTP_BACK_CLICK = "Login_otp_backBtn_click"
        const val LOGIN_OTP_SUBMIT_EVENT = "Login_otp_submit"
        /* END - Login */

        /* Edit Profile */
        const val EDIT_PROFILE_PAGE_PRESENTED = "Editprofile_page_presented"
        const val EDIT_PROFILE_SKIP_CLICK = "Editprofile_skip_click"
        const val EDIT_PROFILE_UPDATED_EVENT = "Editprofile_name_updated"
        const val EDIT_PROFILE_IMAGE_UPDATED = "Editprofile_image_updated"
        const val EDIT_PROFILE_AVATAR_UPDATED = "Editprofile_avatar_updated"
        const val EDIT_PROFILE_GALLERY_PICK = "Editprofile_galleryPick_event"
        const val EDIT_PROFILE_TAKE_PHOTO = "Editprofile_takePhoto_event"
        /* END - Edit Profile */

        /* Profile */
        const val PROFILE_PAGE_PRESENTED = "Profile_page_presented"
        const val PROFILE_BACK_ACTION_CLICK = "Profile_backAction_click"
        const val PROFILE_USER_TITLE_CLICK = "Profile_userTitle_click"
        const val PROFILE_OPTIONS_MY_ACCOUNT_CLICK = "Profile_options_myAccount_click"
        const val PROFILE_OPTIONS_MY_ORDERS_CLICK = "Profile_options_myOrders_click"
        const val PROFILE_OPTIONS_ADDRESSES_CLICK = "Profile_options_addresses_click"
        const val PROFILE_OPTIONS_CART_CLICK = "Profile_options_cart_click"
        const val PROFILE_OPTIONS_LOGOUT_CLICK = "Profile_options_logout_click"
        const val PROFILE_SETTINGS_ICON_CLICK = "Profile_toolbar_settingsIcon_click"
        const val PROFILE_RECENT_ORDERS_ITEM_CLICK = "Profile_recentOrders_item_click"
        const val PROFILE_RECENT_ORDERS_SEE_ALL_CLICK = "Profile_recentOrders_seeAll_click"
        const val PROFILE_RECENT_VIEWS_ITEM_CLICK = "Profile_recentViews_item_click"
        const val PROFILE_RECENT_VIEWS_SEE_ALL_CLICK = "Profile_recentViews_seeAll_click"
        const val PROFILE_WISHLIST_ITEM_CLICK = "Profile_wishlist_item_click"
        const val PROFILE_WISHLIST_SEE_ALL_CLICK = "Profile_wishlist_seeAll_click"
        const val PROFILE_SAVED_ITEMS_ITEM_CLICK = "Profile_savedItems_item_click"
        const val PROFILE_SAVED_ITEMS_SEE_ALL_CLICK = "Profile_savedItems_seeAll_click"
        /* END - Profile */

        /* Best Deals */
        const val BEST_DEALS_PAGE_PRESENTED = "Bestdeals_page_presented"
        const val BEST_DEALS_BANNER_ITEM_CLICK = "Bestdeals_bannerItem_click"
        const val BEST_DEALS_TRENDING_ITEM_CLICK = "Bestdeals_trendingItem_click"
        const val BEST_DEALS_TOP_DEALS_ITEM_CLICK = "Bestdeals_topDealsItem_click"
        /* END - Best Deals */

        /* Search */
        const val SEARCH_PAGE_PRESENTED = "Search_page_presented"
        const val SEARCH_NAVIGATION_ICON_CLICK = "Search_navigationIcon_click"
        const val SEARCH_TRENDING_ITEM_CLICK = "Search_trending_item_click"
        const val SEARCH_TOP_DEALS_ITEM_CLICK = "Search_topDeals_item_click"
        const val SEARCH_SHOPS_RESULT_CLICK = "Search_shopsResult_click"
        const val SEARCH_RESULT_ITEM_CLICK = "Search_productResult_item_click"
        /* END - Search */

        /* Shop Detail */
        const val SHOP_DETAIL_PAGE_PRESENTED = "Shopdetail_page_presented"
        const val SHOP_DETAIL_TOOLBAR_OPTIONS_CLICK = "Shopdetail_toolbarOptions_click"
        const val SHOP_DETAIL_TOOLBAR_SHARE_CLICK = "Shopdetail_toolbarShare_click"
        const val SHOP_DETAIL_CALL_CLICK = "Shopdetail_call_click"
        const val SHOP_DETAIL_VIEW_MAP_CLICK = "Shopdetail_viewMap_click"
        const val SHOP_DETAIL_PRODUCTS_ITEM_CLICK = "Shopdteail_products_item_click"
        const val SHOP_DETAIL_REPORT_ACTION = "Shopdetail_report_action"
        const val SHOP_DETAIL_NAVIGATION_CLICK = "Shopdetail_navigationIcon_click"
        /* END - Shop Detail */

        /* Cart */
        const val CART_PAGE_PRESENTED = "Cart_page_presented"
        const val CART_PROCEED_TO_BUY_BUTTON_CLICK = "Cart_proceedToBuyBtn_click"
        const val CART_TOOLBAR_COUPONS_CLICK = "Cart_toolbar_coupons_click"
        const val CART_ITEM_BUY_NOW_CLICK = "Cart_itemBuyNow_click"
        const val CART_ITEM_QUANTITY_UPDATE = "Cart_itemQuantity_update"
        const val CART_ITEM_QUANTITY_DELETE = "Cart_quantityDelete_click"
        const val CART_ITEM_SAVE_FOR_LATER_CLICK = "Cart_saveForLater_click"
        const val CART_ITEM_ADD_TO_WISHLIST_CLICK = "Cart_addToWishlist_click"
        const val CART_OUT_OF_STOCK_ALERT_PRESENTED = "Cart_outOfStock_alert_presented"
        const val CART_ITEM_REMOVE = "Cart_item_remove"
        const val CART_ITEM_MOVE_TO_CART_CLICK = "Cart_moveToCart_click"
        const val CART_REMOVE_ALERT_SAVE_FOR_LATER_CLICK = "Cart_removeAlert_saveForLater_click"
        const val CART_ITEM_THUMBNAIL_CLICK = "Cart_itemThumbnail_click"
        /* END - Cart */

        /* Coupon Page */
        const val COUPON_PAGE_PRESENTED = "Coupon_page_presented"
        const val COUPON_TAB_CHANGE = "Coupon_tabChange"
        const val COUPON_ITEM_VIEW_COUPCON_CLICK = "Coupon_itemViewCoupon_click"
        const val COUPON_ITEM_MAP_CLICK = "Coupon_itemMap_click"
        const val COUPON_ITEM_CALL_CLICK = "Coupon_itemCall_click"
        const val COUPON_ITEM_CANCEL_ACTION = "Coupon_itemCancel_action"
        const val COUPON_ITEM_THUMBNAIL_CLICK = "Coupon_itemThumbnail_click"
        const val COUPON_NAVIGATION_BTN_CLICK = "Coupon_navigationBtn_click"
        const val VIEW_COUPON_CLOSE_BUTTON_CLICK = "Viewcoupon_closeBtn_click"
        /* END - Coupon Page */

        /* Settings */
        const val SETTIGNS_PAGE_PRESENTED = "Settings_page_presented"
        const val SETTINGS_BACK_ACTION_CLICK = "Settings_backAction_click"
        const val SETTINGS_LOGOUT_CLICK = "Settings_logout_click"
        const val SETTINGS_CHANGE_PIN_CODE_CLICK = "Settings_changePin_click"
        const val SETTINGS_HELP_N_SUPPORT_CLICK = "Settings_helpnSupport_click"
        const val SETTINGS_ABOUT_CLICK = "Settings_about_click"
        const val SETTINGS_TERMS_N_CONDITIONS_CLICK = "Settings_termsnConditions_click"
        const val SETTINGS_DELETE_ACCOUNT_CLICK = "Settings_deleteAccount_click"
        const val SETTINGS_APP_VERSION_CLICK = "Settings_appVersion_click"
        /* END - Settings */

        /* Change pin */
        const val CHANGE_PIN_PAGE_PRESENTED = "Changepin_page_presented"
        const val CHANGE_PIN_UPDATED = "Changepin_updated"
        const val CHANGE_PIN_QUERY = "Changepin_query"
        const val CHANGE_PIN_NAVIGATION_ICON_CLICK = "Changepin_navigationIcon_click"
        /* END - Change pin */

        /* My Address */
        const val MY_ADDRESS_PAGE_PRESENTED = "Myaddress_page_presented"
        const val MY_ADDERSS_ADD_ADDRESS_CLICK = "Myaddress_addAddress_click"
        const val MY_ADDRESS_SAVE_CLICK = "Myaddress_save_click"
        const val MY_ADDRESS_DELETE_ADDRESS_ACTION = "Myaddress_delteAddress_action"
        const val MY_ADDRESS_ITEM_EDIT_CLICK = "Myaddress_itemEditAddress_click"
        const val MY_ADDRESS_ITEM_SET_DEFAULT_CLICK = "Myaddress_itemSetDefault_click"
        const val MY_ADDRESS_NAVIGATION_CLICK = "Myaddress_navigationIcon_click"
        const val MY_ADDRESS_EMPTY_STATE_ADD_ADDRESS_CLICK = "Myaddress_emptyState_addAddress_click"
        /* END - My Address */

        /* Add Address */
        const val ADD_ADDRESS_PAGE_PRESENTED = "Addaddress_page_presented"
        const val ADD_ADDRESS_NEW_ADDRESS_ACTION = "Addaddress_newAddress_action"
        const val ADD_ADDRESS_NAVIGATION_CLICK = "Addaddress_navigationIcon_click"
        /* END - Add Address */

        /* My Orders */
        const val MY_ORDERS_PAGE_PRESENTED = "Myorders_page_presented"
        const val MY_ORDERS_TAB_CHANGE = "Myorders_tabChange"
        const val MY_ORDERS_ITEM_TRACK_ORDER_CLICK = "Myorders_itemTrackOrder_click"
        const val MY_ORDERS_ITEM_VIEW_ORDER_CLICK = "Myorders_itemViewOrder_click"
        const val MY_ORDERS_ITEM_THUMBNAIL_CLICK = "Myorders_itemThumbnail_click"
        const val MY_ORDERS_NAVIGAITON_CLICK = "Myorders_navigationIcon_click"
        /* END - My Orders */

        /* Order detail */
        const val ORDER_DETAIL_PAGE_PRESENTED = "Orderdretail_page_presented"
        const val ORDER_DETAIL_CANCEL_BUTTON_CLICK = "Orderdetail_cancelBtn_click"
        const val ORDER_DETAIL_SELLER_PROFILE_CLICK = "Orderdetail_sellerProfile_click"
        const val ORDER_DETAIL_NAVIGATION_CLICK = "Orderdetail_navigationIcon_click"
        /* END - Order detail */

        /* Home page */
        const val HOME_PAGE_PRESENTED = "Unique_Homepage_presented"
        const val HOME_LOCATION_PIN_CLICK = "Home_locationPin_click"
        const val HOME_NOTIFICATION_CLICK = "Home_notification_click"

        const val HOME_RATE_PRODUCT_ACTION = "Home_rateProduct_action"
        const val HOME_SHARE_PRODUCT_ACTION = "Home_shareProduct_action"
        const val HOME_PRODUCT_OPTIONS_CLICK = "Home_productOptions_click"
        const val HOME_SHOP_ICON_CLICK = "Home_shopIcon_click"
        const val HOME_READ_MORE_TOGGLE = "Home_readMore_toggle"
        const val HOME_VIEW_COUPON_CLICK = "Home_viewCoupon_click"
        const val HOME_ADD_TO_CART_CLICK = "Home_addToCart_click"
        const val HOME_PRODUCT_IMAGES_CLICK = "Home_productImages_click"
        /* END - Home page */

        /* Single listing */
        const val SINGLE_LISTING_PAGE_PRESENTED = "Singlelisting_page_presented"
        const val SINGLE_LISTING_RATE_PRODUCT_ACTION = "Singlelisting_rateProduct_action"
        const val SINGLE_LISTING_SHARE_PRODUCT_ACTION = "Singlelisting_shareProduct_action"
        const val SINGLE_LISTING_PRODUCT_OPTIONS_CLICK = "Singlelisting_productOptions_click"
        const val SINGLE_LISTING_SHOP_ICON_CLICK = "Singlelisting_shopIcon_click"
        const val SINGLE_LISTING_READ_MORE_TOGGLE = "Singlelisting_readMore_toggle"
        const val SINGLE_LISTING_VIEW_COUPON_CLICK = "Singlelisting_viewCoupon_click"
        const val SINGLE_LISTING_ADD_TO_CART_CLICK = "Singlelisting_addToCart_click"
        const val SINGLE_LISTING_PRODUCT_IMAGES_CLICK = "Singlelisting_productImages_click"
        /* END - Single listing */

        /* Checkout */
        const val CHECKOUT_PAGE_PRESENTED = "Checkout_page_presented"
        const val CHECKOUT_NAVIGATION_CLICK = "Checkout_navigationIcon_click"
        const val CHECKOUT_ITEM_REMOVE_CLICK = "Checkout_itemRemove_click"
        const val CHECKOUT_ITEM_QUANTITY_UPDATE = "Checkout_itemQuantity_update"
        const val CHECKOUT_ITEM_QUANTITY_DELETE = "Checkout_quantityDelete_click"
        const val CHECKOUT_PLACE_ORDER_BUTTON_CLICK = "Checkout_placeOrderBtn_click"
        const val CHECKOUT_ADD_ADDRESS_CLICK = "Checkout_addAddress_click"
        const val CHECKOUT_EDIT_ADDRESS_CLICK = "Checkout_editAddress_click"
        const val CHECKOUT_SAVE_ADDRESS_CLICK = "Checkout_saveAddress_click"
        const val CHECKOUT_MAKE_PAYMENT_BUTTON_CLICK = "Checkout_makePaymentBtn_click"
        const val CHECKOUT_DISCARD_PAYMENT_ACTION = "Checkout_discardPayment_action"
        const val CHECKOUT_PAYMENT_SUCCESS_ACTION = "Checkout_payemntSuccess_action"
        const val CHECKOUT_ORDER_SUCCESS_ACTION = "Checkout_orderSuccess_action"
        /* END - Checkout */

        /* View Coupon */
        const val VIEW_COUPON_PAGE_PRESENTED = "Viewcoupon_page_presented"
        const val VIEW_COUPON_VIEW_MAP_CLICK = "Viewcoupon_viewMap_click"
        const val VIEW_COUPON_NAVIGATION_CLICK = "Viewcoupon_navigationIcon_click"
        /* END - View Coupon */

        /* Notification Tab */
        const val NOTICATION_PAGE_PRESENTED = "Notification_page_presented"
        const val TRANSACTIONS_PAGE_PRESENTED = "Transactions_page_presented"
        /* END - Notification Tab */

        /* Notification */
        const val NOTIFICATION_ITEM_CLICK = "Notification_item_click"
        const val NOTIFICATION_MY_ORDERS_ITEM_CLICK = "Notification_myOrderItem_click"
        const val NOTIFICATION_MY_ORDERS_BUTTON_CLICK = "Notification_myOrdersBtn_click"
        const val NOTIFICATION_VIEW_COUPONS_BUTTON_CLICK = "Notificaiton_viewCouponBtn_click"
        const val NOTIFICATION_VIEW_COUPON_ITEM_CLICK = "Notification_couponItem_click"
        const val NOTIFICATION_COUPON_DIALOG_PRESENTED = "Notificaiton_couponDialog_presented"
        const val NOTIFICATION_COPY_COUPON_ACTION = "Notification_copyCoupon_action"
        const val NOTIFICATION_PERMISSION_DENIED = "Notification_permission_deined"
        /* END - Notification */

        /* After first profile upload success */
        const val ONBOARD_SUCCESS_EVENT = "Onboarding_success"

        /* Clicks 'Notifications'  */
        const val NOTIFICATIONS_MENU_EVENT = "Notifications_menu"

        /* Popups */
        const val POPUP_COUPON_PROMOTION_SHOWN = "Pop_couponPromotion_shown"
        /* END - Popups */
    }

    object OtherEvents {
        const val DEEP_LINK_INVITATION = "deepLink_invitation"
    }

    object ErrEvents {
        const val UNCAUGHT_API_FAILURE = "uncaught_api_failure"
    }
}

@Suppress("SpellCheckingInspection")
val ACTIVE_ADJUST_EVENT_TOKEN_MAP = mapOf(
    /* After first profile upload success */
    Analytics.Event.ONBOARD_SUCCESS_EVENT to "x6czwa",
)

@Suppress("SpellCheckingInspection")
val ADJUST_EVENT_TOKEN_MAP = mapOf(
    Analytics.Event.ONBOARD_GET_OTP_BUTTON_EVENT to "8ehms7",
    Analytics.Event.ONBOARD_AUTOFILL_PHONE_NUMBER to "2ncn2w",
    Analytics.Event.ONBOARD_OTP_SUCCESS_EVENT to "p8vepo",
    Analytics.Event.ONBOARD_OTP_FAIL_EVENT to "jdjv78",
    Analytics.Event.ONBOARD_CUSTOMIZED_FIRST_OPEN to "4yiayx",

    /* After first profile upload success */
    Analytics.Event.ONBOARD_SUCCESS_EVENT to "x6czwa",
)

data class AnalyticsEvent(
    @Size(min = 1L, max = 40L) val type: String,
    val params: List<Param> = emptyList()
) {
    class Types {
        companion object {
            const val SCREEN_VIEW = "screen_view" // extras(SCREEN_NAME)
            const val UNCAUGHT_API_FAILURE = "uncaught_api_failure" // extras(API_NAME,PARAMS,COMMENTS)
            const val SEARCH = "search"
        }
    }

    data class Param(val key: String, val value: String)

    class ParamKeys {
        companion object {
            const val SCREEN_NAME       = "screen_name"
            const val API_NAME          = "api_name"
            const val PARAMS            = "params"
            const val COMMENTS          = "comments"
            const val MESSAGE           = "message"
        }
    }
}