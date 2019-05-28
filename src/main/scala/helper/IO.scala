// Copyright (C) 2011-2012 the original author or authors.
// See the LICENCE.txt file distributed with this work for additional
// information regarding copyright ownership.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
package helper

import java.io.{File, FileInputStream, PrintWriter}
import java.time.Instant
import java.nio.file.Files
import org.apache.tika.Tika;
import scala.annotation.tailrec
import scala.concurrent.{Await, Future}
import scala.concurrent.duration._


object IO {

  private val tika:Tika = new Tika

  def writeUTF8File(fname: String, content: String): Unit = {
    val f: File = new File(fname)
    val m = f.getParentFile.mkdirs
    val pw = new PrintWriter(f, "UTF-8")
    pw.print(content)
    pw.close()
  }

  val isExcel: String => Boolean = List("xls", "xlsm", "xlsx").contains(_)

  val isPDF: String => Boolean = List("PDF","pdf").contains(_)

  def uploadDocumentsFromDir(dirs: List[String], filenames: List[String]):Unit = {

    def fileAllowed(file: File): Boolean = Option(file) match {
      case None => false
      case Some(f) => f.exists && f.isFile && f.length > 0
    }

    def makedoc(f: File): String = {
      val name = f.getName
      val fs:FileInputStream = new FileInputStream(f)
      println(s"parse Datei $name")
      println(tika.parseToString(fs))
      "foo"
    }

    @tailrec def scanDirs(dirs: List[File], filelist: List[File]): List[File] = dirs match {
      case Nil => filelist
      case head :: rest => Option(head.listFiles) match {
        case None => scanDirs(rest, filelist)
        case Some(l) => scanDirs(l.filter(_.isDirectory).toList ::: rest, l.filter(f => fileAllowed(f)).toList ::: filelist)
      }
    }

    val df = dirs.map(new File(_)).filter(_.isDirectory)
    val filelist = scanDirs(df, filenames.map(new File(_)).filter(fileAllowed))
    for(f <- filelist) {
      val doc= makedoc(f)
    }
  }
}
