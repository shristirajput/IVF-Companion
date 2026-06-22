import React, { useEffect, useState } from 'react';
import api from '../utils/api';
import { useAuth } from '../context/AuthContext';
import { Heart, MessageCircle, Send, Ghost, FileText, User } from 'lucide-react';

export default function WellnessCommunity() {
  const { user } = useAuth();
  const [posts, setPosts] = useState([]);
  const [loading, setLoading] = useState(true);
  
  const [showNewPost, setShowNewPost] = useState(false);
  const [newPost, setNewPost] = useState({ title: '', content: '', isAnonymous: false });
  const [postLoading, setPostLoading] = useState(false);

  const [activePost, setActivePost] = useState(null);
  const [comments, setComments] = useState([]);
  const [newComment, setNewComment] = useState('');
  const [commentLoading, setCommentLoading] = useState(false);

  useEffect(() => { fetchPosts(); }, []);

  const fetchPosts = async () => {
    try {
      setLoading(true);
      const res = await api.get('/api/forum/posts');
      setPosts(res.data);
    } catch (e) { console.error(e); }
    finally { setLoading(false); }
  };

  const handleCreatePost = async (e) => {
    e.preventDefault();
    try {
      setPostLoading(true);
      await api.post('/api/forum/posts', newPost);
      setNewPost({ title: '', content: '', isAnonymous: false });
      setShowNewPost(false);
      fetchPosts();
    } catch (e) { alert('Failed to create post.'); }
    finally { setPostLoading(false); }
  };

  const openComments = async (post) => {
    if (activePost?.id === post.id) {
      setActivePost(null);
      return;
    }
    setActivePost(post);
    try {
      const res = await api.get(`/api/forum/posts/${post.id}/comments`);
      setComments(res.data);
    } catch (e) { console.error(e); }
  };

  const handleAddComment = async (e) => {
    e.preventDefault();
    if (!newComment.trim() || !activePost) return;
    try {
      setCommentLoading(true);
      await api.post(`/api/forum/posts/${activePost.id}/comments`, { content: newComment });
      setNewComment('');
      const res = await api.get(`/api/forum/posts/${activePost.id}/comments`);
      setComments(res.data);
    } catch (e) { console.error(e); }
    finally { setCommentLoading(false); }
  };

  if (loading && posts.length === 0) {
    return (
      <div className="max-w-[1000px] mx-auto px-6 py-8">
        <div className="sp-skeleton h-10 w-64 mb-4 rounded-xl" />
        <div className="sp-skeleton h-48 w-full mb-6 rounded-xl" />
        <div className="sp-skeleton h-48 w-full rounded-xl" />
      </div>
    );
  }

  return (
    <div className="max-w-[1000px] mx-auto px-6 py-8 animate-fade-in">
      {/* Header */}
      <div className="flex flex-col md:flex-row justify-between items-start md:items-center gap-4 mb-8">
        <div>
          <h1 className="text-3xl font-bold flex items-center gap-3" style={{ color: 'var(--sp-on-surface)', fontFamily: '"Plus Jakarta Sans", sans-serif' }}>
            <div className="w-12 h-12 rounded-xl flex items-center justify-center flex-shrink-0" style={{ background: 'var(--sp-error-container)' }}>
              <Heart className="w-6 h-6" style={{ color: 'var(--sp-error)' }} />
            </div>
            Wellness Community
          </h1>
          <p className="mt-2 text-base" style={{ color: 'var(--sp-on-surface-var)', fontFamily: '"Atkinson Hyperlegible Next", sans-serif' }}>
            A safe space to share journeys, find support, and connect with others.
          </p>
        </div>
        <button
          onClick={() => setShowNewPost(!showNewPost)}
          className="sp-btn-primary"
        >
          <FileText className="w-4 h-4" />
          {showNewPost ? 'Cancel Post' : 'Start Discussion'}
        </button>
      </div>

      {/* New Post Form */}
      {showNewPost && (
        <div className="sp-card p-6 md:p-8 mb-8 animate-fade-in" style={{ border: '1.5px solid var(--sp-primary)' }}>
          <h2 className="text-xl font-bold mb-5" style={{ color: 'var(--sp-on-surface)', fontFamily: '"Plus Jakarta Sans", sans-serif' }}>Create New Post</h2>
          <form onSubmit={handleCreatePost} className="space-y-4">
            <div>
              <label className="sp-label">Title</label>
              <input required type="text" value={newPost.title} onChange={(e) => setNewPost({...newPost, title: e.target.value})} 
                     className="sp-input" placeholder="What's on your mind?" />
            </div>
            <div>
              <label className="sp-label">Content</label>
              <textarea required rows="4" value={newPost.content} onChange={(e) => setNewPost({...newPost, content: e.target.value})} 
                        className="sp-input" placeholder="Share your thoughts..."></textarea>
            </div>
            <div className="flex flex-wrap items-center justify-between gap-4 pt-2">
              <label className="flex items-center gap-2 text-sm font-semibold cursor-pointer" style={{ color: 'var(--sp-on-surface-var)', fontFamily: '"Plus Jakarta Sans", sans-serif' }}>
                <input type="checkbox" checked={newPost.isAnonymous} onChange={(e) => setNewPost({...newPost, isAnonymous: e.target.checked})} 
                       className="w-4 h-4 rounded border-gray-300 focus:ring-primary" style={{ accentColor: 'var(--sp-primary)' }} />
                Post Anonymously
              </label>
              <button type="submit" disabled={postLoading} className="sp-btn-primary px-8">
                {postLoading ? 'Posting...' : 'Publish'}
              </button>
            </div>
          </form>
        </div>
      )}

      {/* Feed */}
      <div className="space-y-6">
        {posts.length > 0 ? (
          posts.map(post => (
            <div key={post.id} className="sp-card p-6 md:p-8 animate-fade-up">
              <div className="flex justify-between items-start gap-4 mb-4">
                <h3 className="text-xl font-bold leading-tight" style={{ color: 'var(--sp-on-surface)', fontFamily: '"Plus Jakarta Sans", sans-serif' }}>{post.title}</h3>
                <span className="text-xs font-semibold whitespace-nowrap" style={{ color: 'var(--sp-outline)', fontFamily: '"Plus Jakarta Sans", sans-serif' }}>
                  {new Date(post.createdAt).toLocaleDateString()}
                </span>
              </div>
              <p className="text-base leading-relaxed mb-5 whitespace-pre-wrap" style={{ color: 'var(--sp-on-surface)', fontFamily: '"Atkinson Hyperlegible Next", sans-serif' }}>
                {post.content}
              </p>
              
              <div className="flex items-center justify-between pt-4 border-t" style={{ borderColor: 'var(--sp-surface-container)' }}>
                <div className="flex items-center gap-2">
                  <div className="w-8 h-8 rounded-full flex items-center justify-center" 
                       style={{ background: post.isAnonymous ? 'var(--sp-surface-highest)' : 'var(--sp-secondary-container)' }}>
                    {post.isAnonymous ? <Ghost className="w-4 h-4" style={{ color: 'var(--sp-on-surface-var)' }} /> : <User className="w-4 h-4" style={{ color: 'var(--sp-secondary)' }} />}
                  </div>
                  <span className="text-sm font-bold" style={{ color: 'var(--sp-on-surface-var)', fontFamily: '"Plus Jakarta Sans", sans-serif' }}>
                    {post.authorName}
                  </span>
                </div>
                <button 
                  onClick={() => openComments(post)}
                  className="sp-btn-secondary text-sm h-10 px-4"
                  style={{ border: 'none', background: 'var(--sp-surface-low)' }}
                >
                  <MessageCircle className="w-4 h-4" />
                  {activePost?.id === post.id ? 'Hide Replies' : 'Discuss'}
                </button>
              </div>

              {/* Comments Section */}
              {activePost?.id === post.id && (
                <div className="mt-6 pt-5 border-t-2 border-dashed animate-fade-in" style={{ borderColor: 'var(--sp-surface-container)' }}>
                  <h4 className="text-sm font-bold mb-4" style={{ color: 'var(--sp-on-surface)', fontFamily: '"Plus Jakarta Sans", sans-serif' }}>Replies</h4>
                  
                  <div className="space-y-3 mb-5 max-h-[400px] overflow-y-auto pr-2">
                    {comments.length > 0 ? (
                      comments.map(c => (
                        <div key={c.id} className="p-4 rounded-xl" style={{ background: 'var(--sp-surface-low)' }}>
                          <p className="text-sm mb-3 leading-relaxed" style={{ color: 'var(--sp-on-surface)', fontFamily: '"Atkinson Hyperlegible Next", sans-serif' }}>{c.content}</p>
                          <div className="flex justify-between items-center text-xs">
                            <span className="font-bold" style={{ color: 'var(--sp-primary)', fontFamily: '"Plus Jakarta Sans", sans-serif' }}>{c.authorName}</span>
                            <span style={{ color: 'var(--sp-outline)' }}>{new Date(c.createdAt).toLocaleDateString()}</span>
                          </div>
                        </div>
                      ))
                    ) : (
                      <p className="text-sm italic" style={{ color: 'var(--sp-outline)' }}>No replies yet. Be the first to share support.</p>
                    )}
                  </div>

                  {user && (
                    <form onSubmit={handleAddComment} className="flex gap-2">
                      <input 
                        type="text" 
                        value={newComment}
                        onChange={(e) => setNewComment(e.target.value)}
                        placeholder="Write a supportive reply..."
                        className="sp-input flex-1"
                        style={{ background: 'var(--sp-surface)' }}
                      />
                      <button 
                        type="submit" 
                        disabled={commentLoading || !newComment.trim()}
                        className="sp-btn-primary px-5"
                      >
                        <Send className="w-4 h-4" />
                      </button>
                    </form>
                  )}
                </div>
              )}
            </div>
          ))
        ) : (
          <div className="sp-card flex flex-col items-center justify-center py-16 text-center">
            <Heart className="w-12 h-12 mb-4" style={{ color: 'var(--sp-outline-var)' }} />
            <p className="text-lg font-bold" style={{ color: 'var(--sp-on-surface)', fontFamily: '"Plus Jakarta Sans", sans-serif' }}>No posts yet</p>
            <p className="text-sm mt-1" style={{ color: 'var(--sp-outline)' }}>Be the first to start a discussion.</p>
          </div>
        )}
      </div>
    </div>
  );
}
