/*
 * Copyright 2007-2011 WorldWide Conferencing, LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.liftweb
package util

import xml._

import org.specs2.mutable.Specification

import common._
import BindHelpers._


/**
 * Systems under specification for BindHelpers.
 */
object BindHelpersSpec extends Specification  {
  "BindHelpers Specification".title

  "the mixinAttributes function" should {
    "mixin in all the attributes" in {
      mixinAttributes(<input />)(<input id="10" class="wee" />) must ==/(<input class="wee" id="10"></input>)
    }

    "not mix in the element label" in {
      mixinAttributes(<input />)(<div id="10" class="wee" />) must ==/(<input class="wee" id="10"></input>)
    }

    "handle the empty cases gracefully" in {
      mixinAttributes(<input />)(<div />) must ==/(<input></input>)
    }

    "not lose existing attributes" in {
      mixinAttributes(<input id="10" />)(<div />) must ==/(<input id="10"></input>)
    }

    "replace attributes with updated values" in {
      mixinAttributes(<input id="10" />)(<div id="12" />) must ==/(<input id="12"></input>)
    }
  }

  "the chooseTemplate function" should {
    "select the node matching a given tag and prefix" in {
      chooseTemplate("choose", "tag", <h><choose:tag a="att1">that</choose:tag></h>) must ==/(Text("that"))
    }
    "select the first node matching a given tag and prefix" in {
      chooseTemplate("choose", "tag", <h><choose:tag>that</choose:tag><choose:tag>those</choose:tag></h>) must ==/(Text("that"))
    }
    "return an empty NodeSeq if no node is found" in {
      chooseTemplate("choose", "tag", <h></h>) must be_==(NodeSeq.Empty)
    }
  }
  "the bind(Map, NodeSeq) function" should {
    "replace the content of a lift:bind node with the content of a map where the key is the value of the attribute 'name'" in {
      val map = Map("hello" -> <h1></h1>, "world" -> <b></b>)
      val liftbind = <body>
        <lift:bind name="hello">changethis</lift:bind>
                     </body>

      bind(map, liftbind) must ==/(<body><h1></h1></body>)
    }

    "bind should not peserve attrs on a bound element" in {

      val res:NodeSeq = bind("ledger", <ledger:entry ledger:id="foo" ledger:class="bar" />, "entry" -> <foo/>)

      res must ==/(<foo/>)
    }
  }
  "the bindlist function" should {
    "replace the content of a lift:bind node with the content of a map where the key is the value of the attribute 'name'" in {
      val maps = List(Map("hello" -> <h1></h1>, "world" -> <b></b>))
      val liftbind = <body>
        <lift:bind name="hello">changethis</lift:bind>
                     </body>
      bindlist(maps, liftbind).get must ==/(<body><h1></h1></body>)
    }
  }
  "the bind(namespace, NodeSeq, BindParams*) function" should {
    "replace a node named 'namespace:bindparam name' in a NodeSeq with the String value of the BindParam" in {
      bind("user", <t><user:tag>replacethis</user:tag></t>, "tag" -> "world") must ==/(<t>world</t>)
    }
    "replace a node named 'namespace:bindparam name' in a NodeSeq with the Symbol value of the BindParam" in {
      bind("user", <t><user:tag>replacethis</user:tag></t>, "tag" -> 'world) must ==/(<t>world</t>)
    }


    "replace a node named 'namespace:bindparam name' in a NodeSeq with the Symbol value of the BindParam via lift:bind attribute" in {
      bind("user", <t><div lift:bind="user:tag">replacethis</div></t>, "tag" -> 'world) must ==/(<t>world</t>)
    }

    "replace a node named 'namespace:bindparam name' in a NodeSeq with the NodeSeq value of the BindParam" in {
      bind("user", <user:tag>replacethis</user:tag>, "tag" -> <world></world>) must ==/(<world></world>)
    }
    "replace a node named 'namespace:bindparam name' in a NodeSeq with the NodeSeq value of the BindParam" in {
      bind("user", <user:tag>replacethis</user:tag>, "tag" -> <world></world>) must ==/(<world></world>)
    }
    "replace a node named 'namespace:bindparam name' in a NodeSeq with the function application of a FuncBindParam" in {
      bind("user", <t><user:tag>hello</user:tag></t>, FuncBindParam("tag", (n: NodeSeq) => Text(n.text + " world"))) must ==/(<t>hello world</t>)
    }
    "properly convert a NodeSeq => NodeSeq to a FuncBindParam" in {
      bind("user", <t><user:tag>hello</user:tag></t>, "tag" -> ((n: NodeSeq) => Text(n.text + " world"))) must ==/(<t>hello world</t>)
    }
    "replace an attribute value named 'namespace:bindparam name' in a NodeSeq with a value from a BindParam" in {
      bind("user", <t user:hello="toreplace"></t>, "hello" -> Text("world")) must ==/(<t user:hello="world"></t>)
    }
    "replace an attribute value named 'namespace:bindparam name' in a NodeSeq with a calculated value from a FuncBindParam" in {
      bind("user", <t user:tag="hello"></t>, FuncBindParam("tag", (n: NodeSeq) => Text(n.text + " world"))) must ==/(<t user:tag="hello world"></t>)
    }
    "replace an attribute named 'namespace:bindparam name' in a NodeSeq with a new attribute name and value from an AttrBindParam" in {
      bind("user", <t user:tag="toreplace"></t>, AttrBindParam("tag", Text("world"), "hello")) must ==/(<t hello="world"></t>)
    }

    "replace an attribute named 'namespace:bindparam name' in a NodeSeq with a new attribute name and value from an AttrBindParam using a String" in {
      bind("user", <t user:tag="toreplace"></t>, AttrBindParam("tag", "world", "hello")) must ==/(<t hello="world"></t>)
    }

    "replace an attribute named 'namespace:bindparam name' in a NodeSeq with a new attribute name and calculated value from an FuncAttrBindParam" in {
      bind("user", <t user:tag="dear"></t>, FuncAttrBindParam("tag", (n: NodeSeq) =>Text(n.text + " world"), "hello")) must ==/(<t hello="dear world"></t>)
    }

    "replace an attribute named 'namespace:bindparam name' in a NodeSeq with a blank attribute name and calculated value from an FuncAttrOptionBindParam" in {
      bind("user", <t user:tag="dear"></t>, FuncAttrOptionBindParam("tag", (n: NodeSeq) => None, "hello")) must ==/(<t/>)
    }


    "replace an attribute named 'namespace:bindparam name' in a NodeSeq with a new attribute name and calculated value from an FuncAttrOptionBindParam" in {
      bind("user", <t user:tag="dear"></t>, FuncAttrOptionBindParam("tag", (n: NodeSeq) => Some(Text(n.text + " world")), "hello")) must ==/(<t hello="dear world"></t>)
    }

    "replace an attribute named 'namespace:bindparam name' in a NodeSeq with a blank attribute name and calculated value from an FuncAttrBoxBindParam" in {
      bind("user", <t user:tag="dear"></t>, FuncAttrBoxBindParam("tag", (n: NodeSeq) => Empty, "hello")) must ==/(<t/>)
    }


    "replace an attribute named 'namespace:bindparam name' in a NodeSeq with a new attribute name and calculated value from an FuncAttrOptionBindParam" in {
      bind("user", <t user:tag="dear"></t>, FuncAttrBoxBindParam("tag", (n: NodeSeq) => Full(Text(n.text + " world")), "hello")) must ==/(<t hello="dear world"></t>)
    }
  }

  "findOption" should {
    "find an id" in {
      val xml = <foo><bar/>Dog<b><woof id="3"/></b></foo>

      findOption(xml) {
        e => e.attribute("id").
        filter(_.text == "3").map(i => e)
      }.get must ==/ (<woof id="3"/>)
    }

    "not find an ide" in {
      val xml = <foo><bar/>Dog<b><woof ide="3"/></b></foo>

      findOption(xml) {
        e => e.attribute("id").
        filter(_.text == "3").map(i => e)
      } must_== None
    }


    "not find a the wrong id" in {
      val xml = <foo><bar/>Dog<b><woof ide="4"/></b></foo>

      findOption(xml) {
        e => e.attribute("id").
        filter(_.text == "3").map(i => e)
      } must_== None
    }
  }

  "findBox" should {
    "find an id" in {
      val xml = <foo><bar/>Dog<b><woof id="3"/></b></foo>

      findBox(xml) {
        e => e.attribute("id").
        filter(_.text == "3").
        map(i => e)
      }.open_! must ==/ (<woof id="3"/>)
    }

    "not find an ide" in {
      val xml = <foo><bar/>Dog<b><woof ide="3"/></b></foo>

      findBox(xml) {
        e => e.attribute("id").
        filter(_.text == "3").map(i => e)
      } must_== Empty
    }


    "not find a the wrong id" in {
      val xml = <foo><bar/>Dog<b><woof ide="4"/></b></foo>

      findBox(xml) {
        e => e.attribute("id").
        filter(_.text == "3").map(i => e)
      } must_== Empty
    }
  }

  "replaceIdNode" should {
    "replace a node" in {
      Helpers.replaceIdNode(<foo><bar id="dog"/></foo>,
                            "dog",
                            <baz/>) must ==/ (<foo><baz id="dog"></baz></foo>)
    }

    "Ignore if no id match" in {
      Helpers.replaceIdNode(<foo><bar/></foo>,
                            "dog",
                            <baz/>) must ==/ (<foo><bar/></foo>)
    }
  }

  "the xmlParam function" should {
    "find the value of an attribute in an xml fragment" in {
      xmlParam(<t hello="world">world</t>, "hello") must_== Full("world")
    }
    "return Empty if the value is empty" in {
      xmlParam(<t hello="">world</t>, "hello") must_== Empty
    }
    "return Empty if the attribute is not found" in {
      xmlParam(<t hello="">world</t>, "notfound") must_== Empty
    }
  }

  "Add CSS Class" should {
    "add a new attribute" in {
      (addCssClass("foo", <b/>) \ "@class").text must_== "foo"
    }

    "append an existing attribute" in {
      (addCssClass("foo", <b class="dog"/>) \ "@class").text must_== "dog foo"
    }

  }

  "The bind helpers should deal correctly with <select>" should {
    "do <select> correctly" in {
      val xhtml = <select name="days"> <stats:options/> </select>

      def options(xhtml: NodeSeq): NodeSeq = {
        <option value="7">week</option><option value="30">month</option> }

      val res = bind("stats", xhtml, "options" -> options _)

      res must ==/(<select name="days"><option value="7">week</option><option value="30">month</option></select>)
    }

    "do <input> correctly" in {
      val xhtml = <input name="days"> <stats:options/> </input>

      def options(xhtml: NodeSeq): NodeSeq = {
        <option value="7">week</option><option value="30">month</option> }

      val res = bind("stats", xhtml, "options" -> options _)

      res must ==/(<input name="days"><option value="7">week</option><option value="30">month</option></input>)
    }

    "do <div> correctly" in {
      val xhtml = <div> <stats:options/> </div>

      def options(xhtml: NodeSeq): NodeSeq = {
        <option value="7">week</option><option value="30">month</option> }

      val res = bind("stats", xhtml, "options" -> options _)
      res must ==/(<div><option value="7">week</option><option value="30">month</option></div>)
    }
  }


  "the bindByName bind(namespace, NodeSeq, BindParams*) function" should {
    "mix in if the element if the type of the elements are the same" in {
      bind("user", <t><input name="user:tag" id="cookie"/></t>, "tag" -> <input name="111222"/>) must ==/(<t><input name="111222" id="cookie"/></t>)
    }
    "replace the element if the replacement element type is not a bindByName type" in {
      bind("user", <t><input name="user:tag" id="cookie"/></t>, "tag" -> "world") must ==/(<t>world</t>)
    }
    "replace the value is value is a null string" in {
      bind("user", <t><input name="user:tag" type="submit" value="press me"/></t>, "tag" -> <input name="111222" type="submit"/>) must ==/(<t><input name="111222" type="submit" value="press me"></input></t>)
    }
    "handle a checkbox" in {
      bind("user", <t><input id="acceptTerms" type="checkbox" name="user:tag"/></t>,
           "tag" -> (<input /> ++
                     <input type="checkbox" name="F893599644556MN4" value="true"/>) ) must ==/(<t><input></input><input name="F893599644556MN4" type="checkbox" value="true" id="acceptTerms"></input></t>)
    }
  }

  "head removal" should {
    "remove <head>" in {
      Helpers.stripHead(<head><i>hello</i></head>) must ==/(<i>hello</i>)
    }

    "ignore non-head" in {
      Helpers.stripHead(<head3><i>hello</i></head3>) must ==/(<head3><i>hello</i></head3>)
    }

    "String subhead" in {
      Helpers.stripHead(<head3><i><head>hello</head></i></head3>) must ==/(<head3><i>hello</i></head3>)
    }
  }

  "Binding attributes" should {
    "handle static, unprefixed attributes" in {
      BindHelpers.bind("test", 
                       <div><div test:x="replace" /></div>,
                       AttrBindParam("x", "staticUnprefixed", "id")) must ==/(<div><div id="staticUnprefixed" /></div>)
    }

    "handle dynamic, unprefixed attributes" in {
      // The Unprefixed attributes that Lift merges in cause the XML equals comparison to fail
      // stringifying and then reparsing fixes it.
      XML.loadString(
        BindHelpers.bind("test", 
                         <div><div test:x="dynamicUnprefixed" /></div>,
                         FuncAttrBindParam("x", {ns : NodeSeq => ns }, "id")).toString) must ==/(<div><div id="dynamicUnprefixed" /></div>)
    }

    "handle static, prefixed attributes" in {
      BindHelpers.bind("test", 
                       <div><div test:x="replace" /></div>,
                       AttrBindParam("x", "staticPrefixed", ("result","id"))) must ==/(<div><div result:id="staticPrefixed" /></div>)
    }

    "handle dynamic, prefixed attributes" in {
      BindHelpers.bind("test", 
                       <div><div test:x="dynamicPrefixed" /></div>,
                       FuncAttrBindParam("x", {ns : NodeSeq => ns}, ("result","id"))) must ==/(<div><div result:id="dynamicPrefixed" /></div>)
    }
  }
}


object CssBindHelpersSpec extends Specification  {

  "css bind helpers" should {
    "clear clearable" in {
      ClearClearable(<b><span class="clearable"/></b>) must ==/ (<b/>)
    }

    "substitute a String by id" in {
      ("#foo" #> "hello").apply(<b><span id="foo"/></b>) must ==/ (<b>hello</b>)
    }


    "not duplicate classes" in {

      def anchor(quesType: String, value: String) = {
        <a href="foo" class="selected">(value)</a>
      }
      var page = 1
      var elements = List("1","2","3","4")
      
      val xml = <div class="lift:Bug.attack bug">
      <div id="question" class="question">
      <a href="#" class="L">1</a>
      <a href="#" class="U">1</a>
      <a href="#" class="D">1</a>
      </div>
      <div class="navigation">
      <button class="previous">Previous</button> <button class="next">Next</button>
      </div>
      </div>
      
      val sel = ".question" #> elements.map(value => {
        ".question [id]" #> ("question-" + value) &
        ".question [class]" #> ("question-" + value) &
        ".L" #> anchor("L", value) &
        ".U" #> anchor("U", value) &
        ".D" #> anchor("D", value)
      })
      
      val res = sel(xml)

      ((res \\ "a").head \ "@class").head.text must_== "selected L"      
    }


    "Compound selector" in {
      val res = 
      (".foo [href]" #> "http://dog.com" & ".bar [id]" #> "moo").apply(
        <a class="foo bar" href="#"/>)
      (res \ "@href").text must_== "http://dog.com"
      (res \ "@id").text must_== "moo"
    }

    "not stack overflow on Elem" in {
      val xf = "* [id]" #> "xx" &
      "* [style]" #> "border:thin solid black" &
      "* *" #> <a/>
      success
    }

    "not stack overflow on Elem" in {
      val xf = "* [id]" #> "xx" &
      "* [style]" #> "border:thin solid black" &
      "* *+" #> <a/>

      xf(<div/>)
      success
    }

    "not stack overflow on Elem" in {
      val xf = "* [id]" #> "xx" &
      "* [style]" #> "border:thin solid black" &
      "* -*" #> <a/>

      xf(<div/>)
      success
    }

    "support modifying attributes along with body" in {
      val org = <a>foo</a>
      val func = "a [href]" #> "dog" & "a *" #> "bar"
      val res = func(org)

      res.toString must_== "<a href=\"dog\">bar</a>"
    }

    "substitute a String by id" in {
      ("#foo" replaceWith "hello").apply(<b><span id="foo"/></b>) must ==/ (<b>hello</b>)
    }

    "substitute a String by nested class" in {
      ("div .foo" #> "hello").apply(<b><div><span class="foo"/></div><span><span class="foo"/></span></b>) must ==/ (<b><div>hello</div><span><span class="foo"/></span></b>)
    }

    "substitute a String by deep nested class" in {
      ("#baz div .foo" #> "hello").apply(
        <b><span id="baz"><div><span class="foo"/></div></span><span><span class="foo"/></span></b>) must ==/ (<b><span id="baz"><div>hello</div></span><span><span class="foo"/></span></b>)
    }

    "insert a String by deep nested class" in {
      ("#baz div .foo *" #> "hello").apply(
        <b><span id="baz"><div><span class="foo"/></div></span><span><div><span class="foo"/></div></span></b>) must ==/ (<b><span id="baz"><div><span class="foo">hello</span></div></span><span><div><span class="foo"/></div></span></b>)
    }



    "Select a node" in {
      ("#foo ^^" #> "hello").apply(<div><span id="foo"/></div>) must ==/ (<span id="foo"/>)
    }

    "Another nested select" in {
      val template = <span>
      <div id="meow">
      <lift:loc locid="asset.import.chooseFile"></lift:loc>
      <span id="file_upload"></span>
      <input type="submit" value="import" /><br></br>
      </div>
      <div id="get">
      <lift:loc locid="asset.import.chooseFile"></lift:loc>
      <span id="file_upload"></span>
      <input type="submit" value="import" /><br></br>
      </div>
      </span>

      val xf = "#get ^^" #> "ignore" & "#file_upload" #> <input type="moose"/>

      val ret = xf(template)

      ret(0).asInstanceOf[Elem].label must_== "div"
      ret.length must_== 1
      (ret \ "@id").text must_== "get"

      (ret \\ "input").length must_== 2

      ((ret \\ "input").toList(0) \ "@type").map(_.text) must_== List("moose")
      
    }

    "Child nested select" in {
      val template = <span>
      <div id="meow">
      <lift:loc locid="asset.import.chooseFile"></lift:loc>
      <span id="file_upload"></span>
      <input type="submit" value="import" /><br></br>
      </div>
      <div id="get">
      <lift:loc locid="asset.import.chooseFile"></lift:loc>
      <span id="file_upload"></span>
      <input type="submit" value="import" /><br></br>
      </div>
      </span>

      val xf = "#get ^*" #> "ignore" & "#file_upload" #> <input type="moose"/>

      val ret = xf(template)

      (ret \\ "div").length must_== 0

      (ret \\ "input").length must_== 2

      ((ret \\ "input").toList(0) \ "@type").map(_.text) must_== List("moose")
      
    }

    "Select a node and transform stuff" in {
      val ret = ("#foo ^^" #> "hello" &
                 "span [id]" #> "bar")(<span id="foo"/>)

      ret(0).asInstanceOf[Elem].label must_== "span"
      ret.length must_== 1
      (ret \ "@id").text must_== "bar"
    }


    "Select a node and transform stuff deeply nested" in {
      val ret = ("#foo ^^" #> "hello" &
                 "span [id]" #> "bar")(<div><div><span id="foo"/></div></div>)

      ret(0).asInstanceOf[Elem].label must_== "span"
      ret.length must_== 1
      (ret \ "@id").text must_== "bar"
    }


    "Select a node and transform stuff deeply nested 2" in {
      val ret = ("#foo ^^" #> "hello" &
                 "span [id]" #> "bar")(<div><div><span id="foo2"/><span id="foo3"/><span dog="woof" id="foo"/></div></div>)

      ret(0).asInstanceOf[Elem].label must_== "span"
      ret.length must_== 1
      (ret \ "@id").text must_== "bar"
      (ret \ "@dog").text must_== "woof"
    }



    "substitute multiple Strings by id" in {
      ("#foo" #> "hello" &
     "#baz" #> "bye"
     )(<b><div id="baz">Hello</div><span id="foo"/></b>) must be_== (NodeSeq fromSeq <b>{Text("bye")}{Text("hello")}</b>)
    }

    "bind href and None content" in {
      val opt: Option[String] = None
      val res = ("top *" #> opt &
                 "top [href]" #> "frog")(<top>cat</top>)

      res.text must_== ""
      (res \ "@href").text.mkString must_== "frog"
    }

    "bind href and Some content" in {
      val opt: Option[String] = Some("Dog")
      val res = ("top *" #> opt &
                 "top [href]" #> "frog")(<top>cat</top>)

      res.text must_== "Dog"
      (res \ "@href").text.mkString must_== "frog"
    }

    "bind href and Some content with multiple attrs" in {
      val opt: Option[String] = Some("Dog")
      val res = ("top *" #> opt &
                 "top [meow]" #> "woof" &
                 "top [href]" #> "frog")(<top href="#">cat</top>)

      res.text must_== "Dog"
      (res \ "@href").text.mkString must_== "frog"
      (res \ "@meow").text.mkString must_== "woof"
    }

    "option transform on *" in {
      val opt: Option[String] = None
      val res = ("* *" #> opt.map(ignore => "Dog")).apply(<top>cat</top>)
      res.head must_== <top></top>
    }

    "append attribute to a class with spaces" in {
      val stuff = List("a", "b")
      val res = ("* [class+]" #> stuff).apply(<top class="q">cat</top>)
      (res \ "@class").text must_== "q a b"
    }

    "append attribute to an href" in {
      val stuff = List("&a=b", "&b=d")
      val res = ("* [href+]" #> stuff).apply(<top href="q?z=r">cat</top>)
      (res \ "@href").text must_== "q?z=r&a=b&b=d"
    }

    "remove an attribute from a class" in {
      val func = ".foo [class!]" #> "andOther"

      (func(<span class="foo andOther" />) \ "@class").text must_== "foo"
    }

    "remove an attribute from a class and the attribute if it's the only one left" in {
      val func = ".foo [class!]" #> "foo"
      val res = func(<span class="foo" />)

      (res \ "@class").length must_== 0
    }



    "Remove a subnode's class attribute" in {

      val func = ".removeme !!" #> ("td [class!]" #> "removeme")
      val res = func.apply(<tr><td class="removeme fish">Hi</td></tr>)

      ((res \ "td") \ "@class").text must_== "fish"
    }


    "not remove a non-existant class" in {
      val func = ".foo [class!]" #> "bar"
      val res = func(<span class="foo" />)

      (res \ "@class").text must_== "foo"
    }


    "remove an attribute from an attribute" in {
      val func = "span [href!]" #> "foo"
      val res = func(<span href="foo" />)

      (res \ "@href").length must_== 0
    }


    "not remove a non-existant href" in {
      val func = "span [href!]" #> "bar"
      val res = func(<span href="foo bar" />)

      (res \ "@href").text must_== "foo bar"
    }
    
    "option transform on *" in {
      val opt: Option[Int] = Full(44)
      val res = ("* *" #> opt.map(ignore => "Dog")).apply(<top>cat</top>)
      res must ==/ (<top>Dog</top>)
    }


    "Java number support" in {
      val f = "a *" #> Full(new java.lang.Long(12))
      val xml = <a>Hello</a>

      f(xml) must ==/ (<a>12</a>)
    }


    "Surround kids" in {
      val f = "a <*>" #> <div></div>
      val xml = <b>Meow <a href="dog">Cat</a> woof</b>

      f(xml) must ==/ (<b>Meow <a href="dog"><div>Cat</div></a> woof</b>)
    }

    "Andreas's thing doesn't blow up" in {
      def cachedMessageList: Box[Box[String]] = Empty

      def messageListId = "Hello"

      def collapseUnless[A](isEmptyCond: Boolean)(f: => A): Box[A] = {
        if (!isEmptyCond) {
          Empty
        } else {
          Full(f)
        }
      }

      ".noMail" #> collapseUnless(cachedMessageList.map(_.isEmpty).openOr(true)) {
                       "tbody [id]" #> messageListId &
                       "*" #> PassThru
                  }

      true must_== true
    }

    "other Andreas test" in {
      def renderBlogEntrySummary = {
        ".blogEntry" #> ((ns: NodeSeq) => {
          ("*" #> "Horse").apply(ns)
        })
      }



      def render = {

        "*" #> ((ns: NodeSeq) =>
          renderBlogEntrySummary.apply(ns) ++ <a>hi</a>
          )
      }

      render

      true must_== true
    }


    "option transform on *" in {
      val opt: Box[String] = Empty
      val res = ("* *" #> opt.map(ignore => "Dog")).apply(<top>cat</top>)
      res.head must_== <top></top>
    }

    "option transform on *" in {
      val opt: Box[Int] = Some(44)
      val res = ("* *" #> opt.map(ignore => "Dog")).apply(<top>cat</top>)
      res must ==/ (<top>Dog</top>)
    }

    "transform on *" in {
      val res = ("* *" #> "Dog").apply(<top>cat</top>)
      res must ==/ (<top>Dog</top>)
    }

    "transform child content on *+" in {
      val res = ("* *+" #> "moose").apply(<a>I like </a>)
      res.text must_== "I like moose"
    }

    "transform child content on -*" in {
      val res = ("* -*" #> "moose").apply(<a> I like</a>)
      res.text must_== "moose I like"
    }

    "transform on li" in {
      val res = ("li *" #> List("Woof", "Bark") & ClearClearable)(
        <ul><li>meow</li><li class="clearable">a</li><li class="clearable">a</li></ul>)
      res must ==/ (<ul><li>Woof</li><li>Bark</li></ul>)
    }

    "substitute multiple Strings by id" in {
      (("#foo" replaceWith "hello") &
       ("#baz" replaceWith "bye")
       )(
         <b><div id="baz">Hello</div><span id="foo"/></b>
      ) must_== (NodeSeq fromSeq <b>{Text("bye")}{Text("hello")}</b>)
    }

    "substitute multiple Strings with a List by id" in {
      ("#foo" #> "hello" &
     "#baz" #> List("bye", "bye"))(<b><div id="baz">Hello</div><span id="foo"/></b>) must_== (NodeSeq fromSeq <b>{Text("bye")}{Text("bye")}{Text("hello")}</b>)
    }

    "substitute multiple Strings with a List by id" in {
      (("#foo" replaceWith "hello") &
       ("#baz" replaceWith List("bye", "bye")))(<b><div id="baz">Hello</div><span id="foo"/></b>) must_== (NodeSeq fromSeq <b>{Text("bye")}{Text("bye")}{Text("hello")}</b>)
    }


    "substitute multiple Strings with a List of XML by id" in {
      val answer = ("#foo" #> "hello" &
     "#baz" #> List[NodeSeq](<i/>, <i>Meow</i>))(<b><div frog="dog" id="baz">Hello</div><span id="foo"/></b>)
      
      (answer \ "i").length must_== 2
      (answer \ "i")(0) must ==/ (<i id="baz" frog="dog"/>)
      (answer \ "i")(1) must ==/ (<i frog="dog">Meow</i>)
    }

    "substitute multiple Strings with a List of XML by id" in {
      val answer = (("#foo" replaceWith "hello") &
                    ("#baz" replaceWith List[NodeSeq](<i/>, <i>Meow</i>)))(<b><div frog="dog" id="baz">Hello</div><span id="foo"/></b>)
      
      (answer \ "i").length must_== 2
      (answer \ "i")(0) must ==/ (<i id="baz" frog="dog"/>)
      (answer \ "i")(1) must ==/ (<i frog="dog">Meow</i>)
    }

    "substitute by name" in {
      val answer = ("name=moose" #> <input name="goof"/>).apply (
        <div><input name="moose" value="start" id="79"/></div>)

      (answer \ "input")(0) must ==/ (<input name="goof" value="start" id="79"/>)
    }
    
    "substitute by name" in {
      val answer = ("name=moose" replaceWith <input name="goof"/>).apply (
        <div><input name="moose" value="start" id="79"/></div>)

      (answer \ "input")(0) must ==/ (<input name="goof" value="start" id="79"/>)
    }
    

    "substitute by name with attrs" in {
      val answer = ("name=moose" #> <input name="goof" value="8" id="88"/>).apply (
        <div><input name="moose" value="start" id="79"/></div>)

      (answer \ "input")(0) must ==/ (<input name="goof" value="8" id="88"/>)
    }
    
    "substitute by name with attrs" in {
      val answer = ("name=moose" replaceWith <input name="goof" value="8" id="88"/>).apply (
        <div><input name="moose" value="start" id="79"/></div>)

      (answer \ "input")(0) must ==/ (<input name="goof" value="8" id="88"/>)
    }
    

    "substitute by a selector with attrs" in {
      val answer = ("cute=moose" #> <input name="goof" value="8" id="88"/>).apply (
        <div><input name="meow" cute="moose" value="start" id="79"/></div>)

      (answer \ "input")(0) must ==/ (<input cute="moose" name="goof" value="8" id="88"/>)
    }
    
    "substitute by a selector with attrs" in {
      val answer = ("cute=moose" replaceWith <input name="goof" value="8" id="88"/>).apply (
        <div><input name="meow" cute="moose" value="start" id="79"/></div>)

      (answer \ "input")(0) must ==/ (<input cute="moose" name="goof" value="8" id="88"/>)
    }

    "Map of funcs" in {
      val func: NodeSeq => NodeSeq = "#horse" #> List(1,2,3).map(".item *" #> _)
      val answer: NodeSeq = func(<span><div id="horse">frog<span class="item">i</span></div></span>)

      answer must ==/ (<span><div id="horse">frog<span class="item">1</span></div><div>frog<span class="item">2</span></div><div>frog<span class="item">3</span></div></span>)
                  
    }

    "maintain unique id attributes provided by transform" in {
      val func = ".thinglist *" #>
        (".thing" #> List("xx1", "xx2", "xx2", "xx2", "xx4").map(t => {
            ".thing [id]" #> t
          })
        )
      val answer = func(<ul class="thinglist"><li id="other" class="thing" /></ul>)

      answer must ==/ (<ul class="thinglist"><li class="thing" id="xx1"></li><li class="thing" id="xx2"></li><li id="other" class="thing"></li><li class="thing"></li><li class="thing" id="xx4"></li></ul>)
    }

    "merge classes" in {
      val answer = ("cute=moose" #> <input class="a" name="goof" value="8" id="88"/>).apply (
        <div><input name="meow" class="b" cute="moose" value="start" id="79"/></div>)

      (answer \ "input")(0) must ==/ (<input class="a b" cute="moose" name="goof" value="8" id="88"/>)
    }
    

    "merge classes" in {
      val answer = ("cute=moose" replaceWith <input class="a" name="goof" value="8" id="88"/>).apply (
        <div><input name="meow" class="b" cute="moose" value="start" id="79"/></div>)

      (answer \ "input")(0) must ==/ (<input class="a b" cute="moose" name="goof" value="8" id="88"/>)
    }
    



    "list of strings" in {
      val answer = ("#moose *" #> List("a", "b", "c", "woof") &
                    ClearClearable).apply (
        <ul>
        <li id="moose">first</li>
        <li class="clearable">second</li>
        <li class="clearable">Third</li>
        </ul>)
        
      val lis = (answer \ "li").toList
      
      lis.length must_== 4

      lis(0) must ==/ (<li id="moose">a</li>)
      lis(3) must ==/ (<li>woof</li>)
    }
    

    "list of Nodes" in {
      val answer = ("#moose *" #> List[NodeSeq](<i>"a"</i>, Text("b"), Text("c"), <b>woof</b>) &
                    ClearClearable).apply (
        <ul>
        <li id="moose">first</li>
        <li class="clearable">second</li>
        <li class="clearable">Third</li>
        </ul>)
        
      val lis = (answer \ "li").toList
      
      lis.length must_== 4

      lis(0) must ==/ (<li id="moose"><i>"a"</i></li>)
      lis(3) must ==/ (<li><b>woof</b></li>)
    }
    

    "set href" in {
      val answer = ("#moose [href]" #> "Hi" &
                    ClearClearable).apply (
        <ul><a id="moose" href="meow">first</a><li class="clearable">second</li><li class="clearable">Third</li></ul>)
        
    
    (answer \ "a" \ "@href").text must_== "Hi"
      (answer \ "li").length must_== 0
    }
    
    "set href and subnodes" in {
      val answer = ("#moose [href]" #> "Hi" &
                    ClearClearable).apply (
        <ul><a id="moose" href="meow">first<li class="clearable">second</li><li class="clearable">Third</li></a></ul>)
        
    
    (answer \ "a" \ "@href").text must_== "Hi"
      (answer \\ "li").length must_== 0
    }


    "list of strings" in {
      val answer = (("#moose *" replaceWith List("a", "b", "c", "woof")) &
                    ClearClearable).apply (
        <ul>
        <li id="moose">first</li>
        <li class="clearable">second</li>
        <li class="clearable">Third</li>
        </ul>)
        
      val lis = (answer \ "li").toList
      
      lis.length must_== 4

      lis(0) must ==/ (<li id="moose">a</li>)
      lis(3) must ==/ (<li>woof</li>)
    }
    
    "bind must bind to subnodes" in {
      val html = <ul class="users">
      <li class="user" userid="">
      <img class="userimg" src=""/>
      </li>
      </ul>

      val lst = List(1,2,3)

      val f = ".users *" #> ("li" #> lst.map(i => ".user [userid]" #> i))

      (f(html) \\ "ul").length must_== 1
      (f(html) \\ "li").length must_== 3
    }

    "list of Nodes" in {
      val answer = (("#moose *" replaceWith List[NodeSeq](<i>"a"</i>, Text("b"), Text("c"), <b>woof</b>)) &
                    ClearClearable).apply (
        <ul>
        <li id="moose">first</li>
        <li class="clearable">second</li>
        <li class="clearable">Third</li>
        </ul>)
        
      val lis = (answer \ "li").toList
      
      lis.length must_== 4

      lis(0) must ==/ (<li id="moose"><i>"a"</i></li>)
      lis(3) must ==/ (<li><b>woof</b></li>)
    }
    

    "set href" in {
      val answer = (("#moose [href]" replaceWith "Hi") &
                    ClearClearable).apply (
        <ul><a id="moose" href="meow">first</a><li class="clearable">second</li><li class="clearable">Third</li></ul>)
        
    
    (answer \ "a" \ "@href").text must_== "Hi"
      (answer \ "li").length must_== 0
    }
    
    "set href and subnodes" in {
      val answer = (("#moose [href]" replaceWith "Hi") &
                    ClearClearable).apply (
        <ul><a id="moose" href="meow">first<li class="clearable">second</li><li class="clearable">Third</li></a></ul>)
        
    
    (answer \ "a" \ "@href").text must_== "Hi"
      (answer \\ "li").length must_== 0
    }
    

  }
}


/**
 * This class doesn't actually perform any tests, but insures that
 * the implicit conversions work correctly
 */
object CheckTheImplicitConversionsForToCssBindPromoter {
  val bog = new ToCssBindPromoter(Empty, Empty)

  import BindHelpers._

  "foo" #> "baz"

  bog #> "Hello" 
  bog #> <span/>
  bog #> 1
  bog #> 'foo
  bog #> 44L
  bog #> false

  bog #> List(<span/>)
  bog #> Full(<span/>)
  bog #> Some(<span/>)


  bog #> List("Hello")
  bog #> Full("Dog")
  bog #> Some("Moo")


  bog #> List((null: Bindable))
  bog #> Full((null: Bindable))
  bog #> Some((null: Bindable))

  bog #> nsToNs _
  bog #> nsToOptNs _
  bog #> nsToBoxNs _
  bog #> nsToSeqNs _

  bog #> nsToString _
  bog #> nsToOptString _
  bog #> nsToBoxString _
  bog #> nsToSeqString _

  val nsf: NodeSeq => NodeSeq = bog #> "Hello" &
  bog #> <span/> &
  bog #> 1 &
  bog #> 'foo &
  bog #> 44L &
  bog #> false

  "foo" #> "Hello" 
  "foo" #> <span/>
  "foo" #> 1
  "foo" #> 'foo
  "foo" #> 44L
  "foo" #> false

  "foo" #> List(<span/>)
  "foo" #> Full(<span/>)
  "foo" #> Some(<span/>)


  "foo" #> List("Hello")
  "foo" #> Full("Dog")
  "foo" #> Some("Moo")


  "foo" #> List((null: Bindable))
  "foo" #> Full((null: Bindable))
  "foo" #> Some((null: Bindable))

  "foo" #> nsToNs _
  "foo" #> nsToOptNs _
  "foo" #> nsToBoxNs _
  "foo" #> nsToSeqNs _

  "foo" #> nsToString _
  "foo" #> nsToOptString _
  "foo" #> nsToBoxString _
  "foo" #> nsToSeqString _

  "#foo" #> Set("a", "b", "c")

  val nsf2: NodeSeq => NodeSeq = "foo" #> "Hello" &
  "foo" #> <span/> &
  "foo" #> 1 &
  "foo" #> 'foo &
  "foo" #> 44L &
  "foo" #> false

  "bar" #> List("1","2","3").map(s => "baz" #> s)

  "bar" #> Full(1).map(s => ("baz" #> s): CssBindFunc)
  "bar" #> Some(1).map(s => ("baz" #> s): CssBindFunc)



  def nsToNs(in: NodeSeq): NodeSeq = in
  def nsToOptNs(in: NodeSeq): Option[NodeSeq] = Some(in)
  def nsToBoxNs(in: NodeSeq): Box[NodeSeq] = Full(in)
  def nsToSeqNs(in: NodeSeq): Seq[NodeSeq] = List(in)

  def nsToString(in: NodeSeq): String = in.text
  def nsToOptString(in: NodeSeq): Option[String] = Some(in.text)
  def nsToBoxString(in: NodeSeq): Box[String] = Full(in.text)
  def nsToSeqString(in: NodeSeq): Seq[String] = List(in.text)
}
