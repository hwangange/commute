OpenStax On The Go
=============================================
With this app, OpenStax textbooks are now...
* PORTABLE. Listen to titles offline for free.
* EASILY ACCESSIBLE. Navigate textbook content quickly.
* EQUIPPED. Enhance your learning experience by streaming audio while following the text.

OpenStax On the Go has the following capabilities and features:
* Psychology and Biology textbooks
* Audio scrubber
* Multiple voices provided by Amazon Polly Text to Speech software
* Highlighted text as it is read out loud
* Favorites
* Day/Night mode
* Toggle between Textbook Reading and Battery Saver screens


## Using Amazon Polly

1. This app requires Cognito to authorize to Amazon Polly in order to access device shadows.  Use Amazon Cognito to create a new identity pool:
	1. In the [Amazon Cognito Console](https://console.aws.amazon.com/cognito/), press the `Manage Federated Identities` button and on the resulting page press the `Create new identity pool` button.
	
	1. Give your identity pool a name and ensure that `Enable access to unauthenticated identities` under the `Unauthenticated identities` section is checked.  This allows the sample application to assume the unauthenticated role associated with this identity pool.  Press the `Create Pool` button to create your identity pool.

		**Important**: see note below on unauthenticated user access.

	1. As part of creating the identity pool, Cognito will setup two roles in [Identity and Access Management (IAM)](https://console.aws.amazon.com/iam/home#roles).  These will be named something similar to: `Cognito_PoolNameAuth_Role` and `Cognito_PoolNameUnauth_Role`.  You can view them by pressing the `View Details` button.  Now press the `Allow` button to create the roles.
	1. Save the `Identity pool ID` value that shows up in red in the "Getting started with Amazon Cognito" page, it should look similar to: `us-east-1:xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx" and note the region that is being used.  These will be used in the application code later.
	1. Now we will attach a policy to the unauthenticated role which has permissions to access the required Amazon Polly API.  This is done by attaching an IAM Policy to the unauthenticated role in the [IAM Console](https://console.aws.amazon.com/iam/home#roles).  First, search for the unauth role that you created in step 3 above (named something similar to `Cognito_PoolNameUnauth_Role`) and select its hyperlink.  In the resulting "Summary" page press the `Attach Policy` button in the "Permissions" tab.
	1. Search for "polly" and check the box next to the policy named `AmazonPollyFullAccess` and then press the `Attach Policy` button.  This policy allows the application to perform all operations on the Amazon Polly service.

		More information on AWS IAM roles and policies can be found [here](http://docs.aws.amazon.com/IAM/latest/UserGuide/access_policies_manage.html).  More information on Amazon Polly policies can be found [here](http://docs.aws.amazon.com/polly/latest/dg/using-identity-based-policies.html).

1. Open `AudioClient.java` and update the following constants with the appropriate values:

    ```
    COGNITO_POOL_ID = "CHANGE_ME";
    MY_REGION = Regions.US_EAST_1;
    ```

1. Build and run the app.