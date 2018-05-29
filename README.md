# Sinicum Server

[![Build Status](https://travis-ci.org/dievision/sinicum-server.svg?branch=master)](https://travis-ci.org/dievision/sinicum-server)

Sinicum Server is a Magnolia CMS Module that exposes [Magnolia
CMS](http://www.magnolia-cms.com) Content via an HTTP REST interface
and allows to preview HTML pages in the Pages app that originate from
external applications.

It was originally developed as the Server-side component of the
[Sinicum](https://github.com/dievision/sinicum) Rails Engine but is
language-neutral and can potentially be used with any other client.


## Requirements

Sinicum Server currently requires Magnolia CMS 5.2 or above.


## Installation

If you have
[set up your Magnolia CMS project via Maven](http://dev.magnolia-cms.com/~gjoseph/dont-build-magnolia-build-your-projects),
include the following dependency:

```xml
<dependency>
  <groupId>com.dievision.sinicum</groupId>
  <artifactId>sinicum-server-magnolia-5</artifactId>
  <version>0.11.2</version>
</dependency>
```

To resolve the dependency, also add Dievision’s Maven Repository to
the list of your repositories:

```xml
<repositories>
  <repository>
    <id>maven.dievision.de.releases</id>
    <name>Dievision Maven Repository</name>
    <url>https://mvn-dievision.s3.amazonaws.com/release</url>
  </repository>
</repositories>
```

If you use Sinicum Server in context of a Rails project, it is
easiest, to just run `rails generate sinicum:install`, as explained in
the [Sinicum](http://github.com/dievision/sinicum) README.


## REST API

The REST API currently exposes four main areas:

* Access Magnolia CMS content
* Convenience functionality for building a site’s navigation
* Provide information about the templating configuration
* Information to help with client-side caching


### Authorization

When the module is installed, a role `sinicum-server` is being
created. Make sure that the user that is used to access the API is
a member of this role. Otherwise, the requests will fail.

Other than that, the usual Magnolia CMS authentication and
authorization rules apply.


### Making a request

By default, all Sinicum Server requests have the path
prefix `sinicum-rest`.

You can access the API e.g. via

    curl -u username:password https://localhost:8080/sinicum-rest/website/path

All requests return JSON by default. You can obtain pretty output by
setting the `pretty` request parameter to `true`,
e.g. `/sinicum-rest/website/path?pretty=true`.


### Accessing Magnolia CMS content

When you make a request for a Magnolia CMS node, the node returns
together with all children that do not have the same Primary Node Type
as the requested Node itself. E.g. when you request a page, the
`mgnl:page` node is returned together with all `mgnl:area` and
`mgnl:component` child nodes, but not with child pages.

Currently, the content API is read only.


#### Get a node and its children by its path

    GET /:workspace/:path


#### Get a node and its children by its UUID

    GET /:workspace/_uuid/:uuid

#### Execute a JCR query and return the resulting nodes with their children

    GET /:workspace/_query

##### Parameters

| Name | Type | Description |
| ---- | ---- | ----------- |
| query | String | The JCR query. |
| language | String | The query language. Allowed are `xpath`, `sql`, `JCR-SQL2`.|
| limit | Long | The maximum size of the result set. |
| offset | Long | The offset of the result set. |

#### Get the binary content of a node property

    GET /:workspace/:workspace/_binary/:path

##### Parameters

| Name | Type | Description |
| ---- | ---- | ----------- |
| property | String | The name of the node’s property containing the binary content. |



### Convenience functionality for building a site’s navigation

#### Get information about a page’s child pages

    GET /_navigation/children/:base_page_path

##### Parameters

| Name | Type | Description |
| ---- | ---- | ----------- |
| properties | String | Comma-separated list with the names of the properties to return for each child page. |
| depth | Integer | Number of hierarchy levels to descend. |


#### Get information about a page’s parent pages

    GET /_navigation/parents/:base_page_path

##### Parameters

| Name | Type | Description |
| ---- | ---- | ----------- |
| properties | String | Comma-separated list with the names of the properties to return for each parent page. |



### Information about the templating configuration

#### Return all components for a page

    GET /_templating/components/:module_name/:page_name

#### Return all components for an area

    GET /_templating/components/:module_name/:page_name/:area_name

#### Create and initialize a new area on a page

    POST /_templating/areas/initialize

##### Parameters

| Name | Type | Description |
| ---- | ---- | ----------- |
| workspace | String | The name of the workspace. |
| base_node_uuid | String | The UUID of the page node to create the area for. |
| area_name | String | The name of the area to create. |


### Helpers for client-side caching

#### Return a fingerprint that changes after a repository change

    GET /_cache/global

This functionality helps a client to decide if cached content can be
used. The returned fingerprint changes every time a change is made to
the `website`, `config`, or `dam` workspaces.


## Proxy Filter

The Proxy filter is installed as a regular Magnolia CMS filter in the
config workspace at `/server/filters/cms/sinicumProxyFilter`.

By default, it proxies all requests that do not go to the Magnolia CMS
administration interface to http://localhost:3000, Rails’ default
development servre URL.

The filter can be customized using the usual Magnolia CMS filter
mechanisms, e.g. via the _bypasses_ functionality and via the
parameters given at `/modules/sinicum-server/config`.

