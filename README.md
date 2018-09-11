# hotswap

Replace loaded class by using java instrument API

usage:
<pre><code>
java -cp &lt;jdk_path&gt;/lib/tools.jar:./hotswap.jar com.github.jvmtool.hotswap.Main &lt;pid&gt; &lt;path_to_class_file&gt;
</code></pre>
or
<pre><code>
java -cp &lt;jdk_path&gt;/lib/tools.jar:./hotswap.jar com.github.jvmtool.hotswap.Main &lt;pid&gt; &lt;path_to_class_file&gt; &lt;class_full_name&gt;
</code></pre>
