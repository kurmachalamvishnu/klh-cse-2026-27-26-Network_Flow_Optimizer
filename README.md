# Network Flow Optimization System (Web Dashboard)

**Course:** Data Structures and Algorithms - 3 (`25CS2103E`)  
**Department:** Computer Science & Engineering, KL University  
**Guidance:** Dr. Ch. Anuradha Mam (Assistant Professor, CSE)  
**Team Members:**
- **K. Vishnu** (2520030199)
- **U. Praneeth** (2520030188)
- **U. Vidahth** (2520030597)

---

## 🌐 Modern Interactive Web Dashboard

The project now includes an **interactive full-stack web application** (website) powered by an embedded Java HTTP REST backend:
- **Zero External Dependencies**: Built entirely with Java SE's embedded `com.sun.net.httpserver.HttpServer`.
- **Frontend (Website)**: Located in `web/` (`index.html`, `style.css`, `app.js`).
- **Interactive Geospatial Map**: Renders the **real-world Sioux Falls road network** with exact coordinates from `SiouxFalls_node.tntp`.
- **Pan, Zoom & Drag**: Full mouse wheel zooming, canvas panning, and node dragging.
- **Neon Red Bottleneck Cuts**: Saturated critical roads from the Min-Cut theorem glow in **bold neon red**.
- **Live Augmentation Player**: Step forward, step backward, or auto-play augmenting paths to watch flow build dynamically!
- **Algorithm Comparison Table**: Side-by-side benchmark comparison between Dinic, Edmonds-Karp, and Ford-Fulkerson with real-time speedup calculation.

---

## 📊 Integrated Datasets (Sioux Falls & Baselines)

1. **`SiouxFalls_net.tntp`**: 24 Zones, 76 Directed Links with physical capacities.
2. **`SiouxFalls_node.tntp`**: Geospatial $(X, Y)$ coordinates for map layout.
3. **`SiouxFalls_trips.tntp`**: 576 Origin-Destination pairs (Total trip demand: **360,600 vehicles**).
4. **12-Node Benchmark Network**: Slide 8 test network from `Project_PPT.pptx`.
5. **Telecom, Urban Traffic, and Pipeline distribution networks**.

---

## 🚀 How to Run the Website

### Option 1: Double-Click (Easiest)
- Double-click **`run_website.bat`**.
  - Starts the embedded Java web server.
  - Automatically launches your browser (Chrome/Edge) at **`http://localhost:8080`**!

### Option 2: In VS Code
1. Open the project folder in VS Code.
2. Press <kbd>Ctrl</kbd> + <kbd>Shift</kbd> + <kbd>D</kbd> (Run and Debug).
3. Select **`🌐 Launch Website Dashboard in Browser (Default)`** and click the green ▶️ button (or press <kbd>F5</kbd>).

### Option 3: From Terminal
```powershell
cd C:\KL\2-1\DSA-3\NetworkFlowOptimization

# Compile and start web server (opens browser automatically)
javac -encoding UTF-8 -d bin src/model/*.java src/algorithms/*.java src/data/*.java src/benchmark/*.java src/web/*.java src/ui/*.java src/Main.java test/*.java
java -cp bin Main
```

### Option 4: Standalone Offline File (No server required)
You can also directly double-click [`web/index.html`](file:///C:/KL/2-1/DSA-3/NetworkFlowOptimization/web/index.html) in Windows File Explorer to open the interactive dashboard directly in any browser!
