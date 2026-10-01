import json
from collections import deque

with open(r'C:\KL\2-1\DSA-3\NetworkFlowOptimization\scratch_sf.json') as f:
    data = json.load(f)

nodes = data['nodes']
edges = data['edges']
n = len(nodes)
adj = [[] for _ in range(n)]
edge_objs = []

for e in edges:
    u, v, cap = e['from'], e['to'], e['capacity']
    fwd = {'from': u, 'to': v, 'capacity': cap, 'flow': 0.0, 'rev': None}
    rev = {'from': v, 'to': u, 'capacity': 0.0, 'flow': 0.0, 'rev': fwd}
    fwd['rev'] = rev
    adj[u].append(fwd)
    adj[v].append(rev)
    edge_objs.append(fwd)

s, t = 0, 23
max_flow = 0.0
paths = []

while True:
    parent_edge = [None] * n
    q = deque([s])
    while q:
        curr = q.popleft()
        if curr == t: break
        for edge in adj[curr]:
            nxt = edge['to']
            res = edge['capacity'] - edge['flow']
            if parent_edge[nxt] is None and nxt != s and res > 1e-6:
                parent_edge[nxt] = edge
                q.append(nxt)
    if parent_edge[t] is None:
        break
    
    curr = t
    bottle = float('inf')
    while curr != s:
        edge = parent_edge[curr]
        bottle = min(bottle, edge['capacity'] - edge['flow'])
        curr = edge['from']
    
    curr = t
    path = []
    while curr != s:
        edge = parent_edge[curr]
        edge['flow'] += bottle
        edge['rev']['flow'] -= bottle
        path.append(f'Zone_{curr+1}')
        curr = edge['from']
    path.append(f'Zone_{s+1}')
    path.reverse()
    max_flow += bottle
    paths.append(f'Iter {len(paths)+1}: ' + ' -> '.join(path) + f' -> Flow = {bottle:.2f}')

visited = [False] * n
q = deque([s])
visited[s] = True
while q:
    curr = q.popleft()
    for edge in adj[curr]:
        nxt = edge['to']
        res = edge['capacity'] - edge['flow']
        if not visited[nxt] and res > 1e-6:
            visited[nxt] = True
            q.append(nxt)

cut_edges = [e for e in edge_objs if visited[e['from']] and not visited[e['to']]]
print(f'Max Flow: {max_flow:.2f}')
print(f'Iterations: {len(paths)}')
print(f'Cut edges count: {len(cut_edges)}')
for ce in cut_edges:
    print(f'  Bottleneck: Zone_{ce["from"]+1} -> Zone_{ce["to"]+1} (Cap: {ce["capacity"]}, Flow: {ce["flow"]:.2f})')
