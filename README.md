# Cocoa Search

Image search android app using Kakao's Image API
[KakaoImageAPI](https://developers.kakao.com/docs/restapi/search#%EC%9D%B4%EB%AF%B8%EC%A7%80-%EA%B2%80%EC%83%89)

## App Features
### Image Search
<img src="./images/image_search.gif" height="696">
Image Search uses Kakao Image Api.  
Recycerview's layout is StaggeredGridLayout.  
Each ImageView's size is recalculated to match the image's aspect ratio.  

### Pagination
<img src="./images/pagination.gif" height="696">
Uses Android Paging Library for pagination.  
Used PagedListAdapter.  

### Image Recognition
<img src="./images/detail.jpeg" height="696">
Modified [Tensorflow-lite image classifcation example](https://github.com/tensorflow/examples/tree/master/lite/examples/image_classification/android) to match the project's needs.  
Get recognitions from image url and shows as a hashtag.  

### HashTag
<img src="./images/hashtag.gif" height="696">
When hashtag is clicked, search result based on hashtag is shown.  

### Visit Website
<img src="./images/website.gif" height="696">
Shows image's website by webview.  

### Share Link
<img src="./images/share_link.gif" height="696">
Can share image website link when clicked.  


## Installation
Clone this repository and import into **Android Studio**
```bash
git clone https://github.com/ahndwon/CocoaSearch.git
```

## Configuration
### KakaoApiKey:
Add the following info to `./local.properties`
```gradle
kakaoApiKey="your-kakao-api-key"
```


## Maintainers
This project is mantained by:
* [Dongwon Ahn](http://github.com/ahndwon)


## Contributing

1. Fork it
2. Create your feature branch (git checkout -b my-new-feature)
3. Commit your changes (git commit -m 'Add some feature')
4. Run the linter (ruby lint.rb').
5. Push your branch (git push origin my-new-feature)
6. Create a new Pull Request

