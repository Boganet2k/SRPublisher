console.log('Start parse script');

var tagTitle = '';
var tagDescription = '';
var facebookDefaultImage = 'false';
var images = [];

console.log('Start parse script 1');

var imgs = document.getElementsByTagName('img');
for (var i = 0; i < imgs.length; i++) {

    if (imgs[i].src && imgs[i].src.indexOf(".svg") == -1 && imgs[i].src.indexOf(".gif") == -1) {
        images.push(imgs[i].src);
        break;
    }

}

console.log('Start parse script 2');

var element = document.querySelector('meta[property="og:image"]');

console.log('Start parse script 2_1');

var ogImage = element && element.getAttribute("content");

console.log('Start parse script 2_2');

if (ogImage != undefined && ogImage.length > 0) {

    console.log('Start parse script 2_3');

    images.splice(0, 0, ogImage);

    console.log('Start parse script 2_4');

    facebookDefaultImage = 'true';

    console.log('Start parse script 2_5');

} else {

    console.log('Start parse script 2_6');

     element = document.querySelector('link[rel="image_src"]');

     console.log('Start parse script 2_7');

     var linkImage = element && element.getAttribute("href");

     console.log('Start parse script 2_8');

     if (linkImage != undefined && linkImage.length > 0) {

         console.log('Start parse script 2_9');

         images.splice(0, 0, linkImage);

         console.log('Start parse script 2_10');
     }
}

console.log('Start parse script 5 ogImage: ' + ogImage);

console.log('Start parse script 3 images.length: ' + images.length + ' images.size: ' + images.size + ' images: ' + images);

for (var i = 0; i < images.length; i++) {
    if (images[i] != undefined) {

        console.log('Start parse script 3_1: ' + images[i] + " indexOf: " + images[i].indexOf);

        if (images[i].indexOf != undefined && images[i].indexOf('//') == 0) {
            images[i] = 'http:' + images[i];
        } else if (images[i].indexOf('http://') == -1 && images[i].indexOf('https://') == -1) {
            images[i] = document.URL + images[i];
        }
    }
}

console.log('Start parse script 4');

element = document.querySelector('meta[property="og:description"]');
var ogDescription = element && element.getAttribute("content");

if (ogDescription != undefined && ogDescription.length > 0) {
    tagDescription = ogDescription;
} else {

    element = document.querySelector('meta[name="description"]');
    var description = element && element.getAttribute("content");

    if (description != undefined && description.length > 0) {
        tagDescription = description;
    }
}

console.log('Start parse script 5');

element = document.querySelector('meta[property="og:title"]');
var ogTitle = element && element.getAttribute("content");

if (ogTitle != undefined && ogTitle.length > 0) {
    tagTitle = ogTitle;
} else {

    var element = document.querySelector('title');
    var title = element && element.text;

    if (title != undefined && title.length > 0) {
        tagTitle = title;
    }
}

console.log('Start parse script 6');

CallToAnAndroidFunction.toJava(images, tagTitle, tagDescription, facebookDefaultImage);